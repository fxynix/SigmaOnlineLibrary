package library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import library.exception.NotFoundException;
import library.exception.TooQuicklyException;
import library.service.LogService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logs")
@Tag(name = "Logs", description = "API for managing logs")
public class LogController {
    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @PostMapping("/{date}")
    @Operation(summary = "Generate logs for date",
            description = "Generate logs for selected date")
    @ApiResponses (value = {
        @ApiResponse(responseCode = "202", description = "Task started successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date")
    })
    public ResponseEntity<String> generateLogsByDate(
            @Parameter(description = "Date in format yyyy-MM-dd",
                    example = "2025-05-02",
                    required = true)
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        CompletableFuture<String> future = logService.generateLogFileForDateAsync(date.toString());
        String taskId = future.join();
        return new ResponseEntity<>("Task ID: " + taskId, HttpStatus.ACCEPTED);
    }

    @GetMapping("/{taskId}/status")
    @Operation(summary = "Get task's status by ID",
            description = "Retrieves task's status by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status is retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Task wasn't found")
    })
    public ResponseEntity<String> getTaskStatus(@PathVariable String taskId) {
        return ResponseEntity.ok("Status: " + logService.getTaskStatus(taskId));
    }

    @GetMapping("/{taskId}/file")
    @Operation(summary = "Get logs by task's ID",
            description = "Retrieves logs by task's ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logs are retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Task wasn't found"),
        @ApiResponse(responseCode = "425", description = "Task wasn't finished"),
    })
    public ResponseEntity<Resource> getLogFileById(
            @Parameter(description = "Task's ID", required = true)
            @PathVariable String taskId) {
        String status = logService.getTaskStatus(taskId);
        if (status.equals("PROCESSING")) {
            throw new TooQuicklyException("Task wasn't finished");
        }
        if (status.startsWith("FAILED")) {
            throw new NotFoundException("Task failed. Logs wasn't generated");
        }

        try {
            String filePath = logService.getLogFilePath(taskId);
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new NotFoundException("Logs wasn't found");
            }
            String date = path.getFileName().toString().substring(8, 18);
            Resource resource = new InputStreamResource(Files.newInputStream(path));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=library-" + date + ".log")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

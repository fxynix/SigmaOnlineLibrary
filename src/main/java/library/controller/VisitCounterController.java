package library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import library.service.VisitCounterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/visits")
@Tag(name = "Visit Counter Controller", description = "API for managing visits")
public class VisitCounterController {
    private final VisitCounterService visitCounterService;

    public VisitCounterController(VisitCounterService visitCounterService) {
        this.visitCounterService = visitCounterService;
    }

    @GetMapping("/count")
    @Operation(summary = "Get count of all requests on URL",
            description = "Retrieves count of all requests by URL")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved count")
    public ResponseEntity<Integer> getVisitCount(
            @Parameter(description = "Site's URL", example = "/api/books")
            @RequestParam String url) {
        return ResponseEntity.ok(visitCounterService.getVisitCount(url));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all counters on every URL",
            description = "Retrieves all counters on every URL")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved counters")
    public ResponseEntity<Map<String, Integer>> getAllVisitCounts() {
        return ResponseEntity.ok(visitCounterService.getAllVisitCounts());

    }

    @DeleteMapping("/reset")
    @Operation(summary = "Reset all counters")
    @ApiResponse(responseCode = "204", description = "All counters were reset")
    public ResponseEntity<Void> resetAllVisitCounts() {
        visitCounterService.resetAllVisitCounts();
        return ResponseEntity.noContent().build();
    }
}

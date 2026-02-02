package library.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import library.exception.NotFoundException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class LogService {
    private static final String LOG_FILE_PATH = "logs/library.log";
    private final Map<String, String> logFiles = new ConcurrentHashMap<>();
    private final Map<String, String> taskStatus = new ConcurrentHashMap<>();

    @Async
    public CompletableFuture<String> generateLogFileForDateAsync(String date) {
        String taskId = UUID.randomUUID().toString();
        taskStatus.put(taskId, "PROCESSING");

        CompletableFuture.runAsync(() -> {

            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                taskStatus.put(taskId, "ERROR: Task Failed");
                return;
            }

            try {
                Path sourcePath = Paths.get(LOG_FILE_PATH);
                if (!Files.exists(sourcePath)) {
                    Files.createDirectory(sourcePath);
                }

                List<String> filteredLines;
                try (Stream<String> lines = Files.lines(sourcePath)) {
                    filteredLines = lines
                            .filter(line -> line.startsWith(date))
                            .toList();
                }

                if (filteredLines.isEmpty()) {
                    taskStatus.put(taskId, "FAILED: Logs wasn't found with date: " + date);
                    throw new NotFoundException("Logs wasn't found with date: " + date);
                }

                Files.createDirectories(Paths.get("logs/"));
                String filename = "logs/" + "library-" + date + ".log";
                Files.write(Paths.get(filename), filteredLines);

                logFiles.put(taskId, filename);
                taskStatus.put(taskId, "COMPLETED");
            } catch (Exception e) {
                String errorMsg = e.getMessage();
                taskStatus.put(taskId, "FAILED: " + errorMsg);
            }
        });

        return CompletableFuture.completedFuture(taskId);
    }

    public String getLogFilePath(String taskId) {
        return logFiles.get(taskId);
    }

    public String getTaskStatus(String taskId) {
        String status = taskStatus.getOrDefault(taskId, "NOT FOUND TASK");
        if (status.equals("NOT FOUND TASK")) {
            throw new NotFoundException("Task wasn't found with ID = " + taskId);
        }
        return status;
    }
}
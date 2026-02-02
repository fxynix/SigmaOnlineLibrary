package library.exception;

public class LogFileNotFoundException extends RuntimeException {
    public LogFileNotFoundException(String message) {
        super(message);
    }
}

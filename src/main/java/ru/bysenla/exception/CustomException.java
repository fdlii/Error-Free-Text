package ru.bysenla.exception;

import java.time.LocalDateTime;

public class CustomException extends RuntimeException {
    private final String errorCode;
    private final LocalDateTime timestamp;
    private final String path;

    public CustomException(String message, String errorCode, LocalDateTime timestamp, String path) {
        super(message);
        this.errorCode = errorCode;
        this.timestamp = timestamp;
        this.path = path;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getPath() {
        return path;
    }
}

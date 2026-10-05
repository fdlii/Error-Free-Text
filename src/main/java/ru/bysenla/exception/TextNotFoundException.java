package ru.bysenla.exception;

import java.time.LocalDateTime;

public class TextNotFoundException extends CustomException {
    public TextNotFoundException(String message, String errorCode, LocalDateTime timestamp, String path) {
        super(message, errorCode, timestamp, path);
    }
}

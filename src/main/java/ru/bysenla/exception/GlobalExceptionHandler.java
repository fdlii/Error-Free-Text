package ru.bysenla.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> {
                    fieldErrors.put(err.getField(), err.getDefaultMessage());
                });
        ErrorResponse errorResponse = new ErrorResponse("Invalid parameters were passed to the request.",
                                                        "400",
                                                        LocalDateTime.now(),
                                                        "",
                                                        fieldErrors);
        log.debug("Validation error occurred.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(TextNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTextNotFoundException(TextNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(),
                                                        ex.getErrorCode(),
                                                        ex.getTimestamp(),
                                                        ex.getPath(),
                                                        null);
        log.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    private record ErrorResponse(String errorMessage, String errorCode, LocalDateTime timestamp, String path, Map<String, String> payload){}
}

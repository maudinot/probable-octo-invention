package com.github.maudinot.octo_invention.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FileValidationException.class)
    public String handleFileValidationException(FileValidationException e) {
        log.error("Invalid file: {}", e.getMessage(), e);
        return e.getMessage();
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception e) {
        log.error("Generic error: {}", e.getMessage(), e);
        return "Internal Server Error";
    }
}

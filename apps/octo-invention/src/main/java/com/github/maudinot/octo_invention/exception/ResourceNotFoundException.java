package com.github.maudinot.octo_invention.exception;

import lombok.Getter;

public class ResourceNotFoundException extends RuntimeException {
    @Getter private final int code;

    public ResourceNotFoundException(String message) {
        super(message);
        code = 0;
    }

    public ResourceNotFoundException(String message, int code) {
        super(message);
        this.code = code;
    }
}

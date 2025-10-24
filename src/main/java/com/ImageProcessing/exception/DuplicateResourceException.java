package com.ImageProcessing.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DuplicateResourceException extends RuntimeException {
    private final HttpStatus status;

    public DuplicateResourceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
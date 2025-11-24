package com.poker.shared.domain.exception;

import java.io.Serial;

/**
 * Base exception for all domain-related errors.
 * Extends RuntimeException to avoid polluting domain logic with checked exceptions.
 */
public class DomainException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;
    
    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}

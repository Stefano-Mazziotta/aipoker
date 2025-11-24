package com.poker.shared.domain.exception;

/**
 * Base exception for all domain-related errors.
 * Extends RuntimeException to avoid polluting domain logic with checked exceptions.
 */
public class DomainException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}

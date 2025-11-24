package com.poker.shared.domain.exception;

/**
 * Exception thrown when domain validation rules are violated.
 */
public class ValidationException extends DomainException {
    private static final long serialVersionUID = 1L;
    
    public ValidationException(String message) {
        super(message);
    }
}

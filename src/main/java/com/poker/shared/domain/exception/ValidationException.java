package com.poker.shared.domain.exception;

import java.io.Serial;

/**
 * Exception thrown when domain validation rules are violated.
 */
public class ValidationException extends DomainException {
    @Serial
    private static final long serialVersionUID = 1L;
    
    public ValidationException(String message) {
        super(message);
    }
}

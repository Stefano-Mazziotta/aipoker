package com.poker.game.domain.exception;

import com.poker.shared.domain.exception.DomainException;

import java.io.Serial;

/**
 * Exception thrown when an illegal player action is attempted.
 */
public class IllegalActionException extends DomainException {
    @Serial
    private static final long serialVersionUID = 1L;
    
    public IllegalActionException(String message) {
        super(message);
    }
}

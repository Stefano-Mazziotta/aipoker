package com.poker.game.domain.exception;

import com.poker.shared.domain.exception.DomainException;

/**
 * Exception thrown when an invalid game state transition is attempted.
 */
public class InvalidGameStateException extends DomainException {
    private static final long serialVersionUID = 1L;
    
    public InvalidGameStateException(String message) {
        super(message);
    }
}

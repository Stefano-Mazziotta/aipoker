package com.poker.shared.domain.valueobject;

/**
 * Immutable value object representing a playing card suit.
 * Part of the shared domain - used across all poker features.
 */
public enum Suit {
    HEARTS("♥"),
    DIAMONDS("♦"),
    CLUBS("♣"),
    SPADES("♠");

    private final String symbol;

    Suit(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }
}

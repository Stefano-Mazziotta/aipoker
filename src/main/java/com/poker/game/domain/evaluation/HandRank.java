package com.poker.game.domain.evaluation;

/**
 * Enum representing poker hand rankings in descending order of strength.
 * Each rank has a numeric value for comparison.
 * 
 * REUSED from existing implementation - proven design.
 */
public enum HandRank {
    STRAIGHT_FLUSH(9, "Straight Flush"),
    FOUR_OF_A_KIND(8, "Four of a Kind"),
    FULL_HOUSE(7, "Full House"),
    FLUSH(6, "Flush"),
    STRAIGHT(5, "Straight"),
    THREE_OF_A_KIND(4, "Three of a Kind"),
    TWO_PAIR(3, "Two Pair"),
    ONE_PAIR(2, "One Pair"),
    HIGH_CARD(1, "High Card");

    private final int value;
    private final String displayName;

    HandRank(int value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public int getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

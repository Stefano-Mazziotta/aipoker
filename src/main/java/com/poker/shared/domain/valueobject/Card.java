package com.poker.shared.domain.valueobject;

import java.util.Objects;

/**
 * Immutable value object representing a playing card.
 * Part of the shared domain - fundamental building block of poker.
 * 
 * REUSED from existing implementation - proven, well-tested design.
 */
public class Card {
    private final Suit suit;
    private final Rank rank;

    public Card(Rank rank, Suit suit) {
        this.rank = Objects.requireNonNull(rank, "Rank cannot be null");
        this.suit = Objects.requireNonNull(suit, "Suit cannot be null");
    }

    public Suit getSuit() { 
        return suit; 
    }
    
    public Rank getRank() { 
        return rank; 
    }

    @Override
    public String toString() {
        // Returns something like "A♥" or "10♠"
        return rank.toString() + suit.toString();
    }
    
    /**
     * Parse a card from its string representation (e.g., "A♥", "10♠")
     */
    public static Card fromString(String cardString) {
        if (cardString == null || cardString.isEmpty()) {
            return null;
        }
        
        // Extract suit (last character)
        String suitStr = cardString.substring(cardString.length() - 1);
        Suit suit = Suit.fromSymbol(suitStr);
        
        // Extract rank (everything except last character)
        String rankStr = cardString.substring(0, cardString.length() - 1);
        Rank rank = Rank.fromString(rankStr);
        
        return new Card(rank, suit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card)) return false;
        Card card = (Card) o;
        return suit == card.suit && rank == card.rank;
    }

    @Override
    public int hashCode() {
        return Objects.hash(suit, rank);
    }
}

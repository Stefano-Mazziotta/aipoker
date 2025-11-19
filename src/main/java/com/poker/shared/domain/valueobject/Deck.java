package com.poker.shared.domain.valueobject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Domain entity representing a standard 52-card deck.
 * Part of the shared domain - used in game management.
 * 
 * REUSED from existing implementation with minor enhancements.
 */
public class Deck {
    private final List<Card> cards;

    public Deck() {
        this.cards = generateAllCards();
    }

    private List<Card> generateAllCards() {
        List<Card> cards = new ArrayList<>(52);
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(rank, suit));
            }
        }
        return cards;
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card dealCard() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Cannot deal from empty deck");
        }
        return cards.remove(0);
    }

    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    public int size() {
        return cards.size();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }
}

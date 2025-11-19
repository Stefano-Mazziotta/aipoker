package com.poker.player.domain.model;

import com.poker.shared.domain.valueobject.Card;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Value Object representing a player's hole cards (private hand).
 * Immutable.
 */
public class PlayerHand {
    private final List<Card> cards;

    private PlayerHand(List<Card> cards) {
        this.cards = List.copyOf(cards);
    }

    public static PlayerHand empty() {
        return new PlayerHand(new ArrayList<>());
    }

    public static PlayerHand of(List<Card> cards) {
        if (cards.size() > 2) {
            throw new IllegalArgumentException("Texas Hold'em player hand cannot have more than 2 cards");
        }
        return new PlayerHand(cards);
    }

    public PlayerHand addCard(Card card) {
        if (cards.size() >= 2) {
            throw new IllegalStateException("Cannot add more than 2 cards to player hand");
        }
        List<Card> newCards = new ArrayList<>(cards);
        newCards.add(card);
        return new PlayerHand(newCards);
    }

    public List<Card> getCards() {
        return cards;
    }

    public int size() {
        return cards.size();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    @Override
    public String toString() {
        return cards.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerHand)) return false;
        PlayerHand that = (PlayerHand) o;
        return cards.equals(that.cards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cards);
    }
}

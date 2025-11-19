package com.poker.game.domain.model;

import com.poker.shared.domain.exception.ValidationException;
import java.util.Objects;

/**
 * Value Object representing the pot (accumulated bets) in a poker game.
 * Immutable with validation.
 */
public class Pot {
    private final int amount;

    private Pot(int amount) {
        if (amount < 0) {
            throw new ValidationException("Pot amount cannot be negative: " + amount);
        }
        this.amount = amount;
    }

    public static Pot empty() {
        return new Pot(0);
    }

    public static Pot of(int amount) {
        return new Pot(amount);
    }

    public Pot add(int bet) {
        if (bet < 0) {
            throw new ValidationException("Cannot add negative amount to pot: " + bet);
        }
        return new Pot(this.amount + bet);
    }

    public int getAmount() {
        return amount;
    }

    public boolean isEmpty() {
        return amount == 0;
    }

    @Override
    public String toString() {
        return "$" + amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pot)) return false;
        Pot pot = (Pot) o;
        return amount == pot.amount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }
}

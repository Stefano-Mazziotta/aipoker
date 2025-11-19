package com.poker.shared.domain.valueobject;

import com.poker.shared.domain.exception.ValidationException;
import java.util.Objects;

/**
 * Value Object representing poker chips (money).
 * Immutable and ensures non-negative values.
 */
public class Chips {
    private final int amount;

    private Chips(int amount) {
        if (amount < 0) {
            throw new ValidationException("Chips amount cannot be negative: " + amount);
        }
        this.amount = amount;
    }

    public static Chips of(int amount) {
        return new Chips(amount);
    }

    public static Chips zero() {
        return new Chips(0);
    }

    public int getAmount() {
        return amount;
    }

    public Chips add(int value) {
        return new Chips(this.amount + value);
    }

    public Chips subtract(int value) {
        if (value > this.amount) {
            throw new ValidationException(
                "Cannot subtract " + value + " from " + this.amount + " chips"
            );
        }
        return new Chips(this.amount - value);
    }

    public boolean canAfford(int cost) {
        return this.amount >= cost;
    }

    public boolean isZero() {
        return this.amount == 0;
    }

    @Override
    public String toString() {
        return "$" + amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Chips)) return false;
        Chips chips = (Chips) o;
        return amount == chips.amount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }
}

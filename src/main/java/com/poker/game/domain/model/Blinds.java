package com.poker.game.domain.model;

import com.poker.shared.domain.exception.ValidationException;
import java.util.Objects;

/**
 * Value Object representing blinds (forced bets) in poker.
 * Immutable with validation.
 */
public class Blinds {
    private final int smallBlind;
    private final int bigBlind;

    public Blinds(int smallBlind, int bigBlind) {
        if (smallBlind <= 0) {
            throw new ValidationException("Small blind must be positive: " + smallBlind);
        }
        if (bigBlind <= 0) {
            throw new ValidationException("Big blind must be positive: " + bigBlind);
        }
        if (bigBlind < smallBlind) {
            throw new ValidationException("Big blind must be >= small blind");
        }
        this.smallBlind = smallBlind;
        this.bigBlind = bigBlind;
    }

    public static Blinds of(int smallBlind, int bigBlind) {
        return new Blinds(smallBlind, bigBlind);
    }

    public static Blinds standard() {
        return new Blinds(10, 20);
    }

    public int getSmallBlind() {
        return smallBlind;
    }

    public int getBigBlind() {
        return bigBlind;
    }

    @Override
    public String toString() {
        return smallBlind + "/" + bigBlind;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Blinds)) return false;
        Blinds blinds = (Blinds) o;
        return smallBlind == blinds.smallBlind && bigBlind == blinds.bigBlind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(smallBlind, bigBlind);
    }
}

package com.poker.game.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing a unique game identifier.
 * Immutable and based on UUID.
 */
public class GameId {
    private final UUID value;

    private GameId(UUID value) {
        this.value = Objects.requireNonNull(value, "GameId value cannot be null");
    }

    public static GameId generate() {
        return new GameId(UUID.randomUUID());
    }

    public static GameId from(String id) {
        try {
            return new GameId(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid GameId format: " + id, e);
        }
    }

    public static GameId from(UUID uuid) {
        return new GameId(uuid);
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameId)) return false;
        GameId gameId = (GameId) o;
        return value.equals(gameId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}

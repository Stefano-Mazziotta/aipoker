package com.poker.player.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing a unique player identifier.
 * Immutable and based on UUID.
 */
public class PlayerId {
    private final UUID value;

    private PlayerId(UUID value) {
        this.value = Objects.requireNonNull(value, "PlayerId value cannot be null");
    }

    public static PlayerId generate() {
        return new PlayerId(UUID.randomUUID());
    }

    public static PlayerId from(String id) {
        try {
            return new PlayerId(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid PlayerId format: " + id, e);
        }
    }

    public static PlayerId from(UUID uuid) {
        return new PlayerId(uuid);
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
        if (!(o instanceof PlayerId)) return false;
        PlayerId playerId = (PlayerId) o;
        return value.equals(playerId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}

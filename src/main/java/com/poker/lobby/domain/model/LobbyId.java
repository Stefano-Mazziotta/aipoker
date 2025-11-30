package com.poker.lobby.domain.model;

import java.util.UUID;

/**
 * Lobby identifier value object.
 */
public class LobbyId {
    private final String value;

    public LobbyId(String value) {
        this.value = value;
    }

    public static LobbyId generate() {
        return new LobbyId(UUID.randomUUID().toString());
    }

    public static LobbyId from(String value) {
        return new LobbyId(value);
    }

    public String getValue() {
        return value;
    }    

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LobbyId)) return false;
        LobbyId lobbyId = (LobbyId) o;
        return value.equals(lobbyId.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}

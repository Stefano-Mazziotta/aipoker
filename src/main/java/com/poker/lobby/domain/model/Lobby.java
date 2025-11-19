package com.poker.lobby.domain.model;

import com.poker.player.domain.model.PlayerId;
import java.util.*;

/**
 * Lobby aggregate root (stub for future implementation).
 * Represents a waiting room where players gather before starting a game.
 */
public class Lobby {
    private final LobbyId id;
    private final String name;
    private final List<PlayerId> players;
    private final int maxPlayers;
    private boolean started;

    public Lobby(LobbyId id, String name, int maxPlayers) {
        this.id = id;
        this.name = name;
        this.maxPlayers = maxPlayers;
        this.players = new ArrayList<>();
        this.started = false;
    }

    public static Lobby create(String name, int maxPlayers) {
        return new Lobby(LobbyId.generate(), name, maxPlayers);
    }

    public void addPlayer(PlayerId playerId) {
        if (players.size() >= maxPlayers) {
            throw new IllegalStateException("Lobby is full");
        }
        if (started) {
            throw new IllegalStateException("Lobby already started");
        }
        players.add(playerId);
    }

    public void removePlayer(PlayerId playerId) {
        players.remove(playerId);
    }

    public void start() {
        if (players.size() < 2) {
            throw new IllegalStateException("Need at least 2 players to start");
        }
        this.started = true;
    }

    public boolean isOpen() {
        return !started && players.size() < maxPlayers;
    }

    // Getters
    public LobbyId getId() { return id; }
    public String getName() { return name; }
    public List<PlayerId> getPlayers() { return List.copyOf(players); }
    public int getMaxPlayers() { return maxPlayers; }
    public boolean isStarted() { return started; }
}

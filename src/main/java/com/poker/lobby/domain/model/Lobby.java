package com.poker.lobby.domain.model;

import java.util.ArrayList;
import java.util.List;

import com.poker.player.domain.model.PlayerId;

/**
 * Lobby aggregate root (stub for future implementation).
 * Represents a waiting room where players gather before starting a game.
 */
public class Lobby {
    private final LobbyId id;
    private final String name;
    private final List<PlayerId> players;
    private final int maxPlayers;
    private final PlayerId adminPlayerId;
    private boolean started;

    public Lobby(LobbyId id, String name, int maxPlayers, PlayerId adminPlayerId) {
        this.id = id;
        this.name = name;
        this.maxPlayers = maxPlayers;
        this.adminPlayerId = adminPlayerId;
        this.players = new ArrayList<>();
        this.players.add(adminPlayerId); // Admin auto-joins
        this.started = false;
    }

    public static Lobby create(String name, int maxPlayers, PlayerId adminPlayerId) {
        return new Lobby(LobbyId.generate(), name, maxPlayers, adminPlayerId);
    }

    public void addPlayer(PlayerId playerId) {
        if (players.contains(playerId)) {
            return; // Player already in lobby
        }
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

    public void start(PlayerId requestingPlayerId) {
        if (!isAdmin(requestingPlayerId)) {
            throw new IllegalStateException("Only admin can start the game");
        }
        if (players.size() < 2) {
            throw new IllegalStateException("Need at least 2 players to start");
        }
        this.started = true;
    }
    
    public boolean isAdmin(PlayerId playerId) {
        return adminPlayerId.equals(playerId);
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
    public PlayerId getAdminPlayerId() { return adminPlayerId; }
}

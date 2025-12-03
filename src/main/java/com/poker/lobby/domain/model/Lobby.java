package com.poker.lobby.domain.model;

import java.util.ArrayList;
import java.util.List;

import com.poker.player.domain.model.Player;
import com.poker.player.domain.model.PlayerId;

/**
 * Lobby aggregate root.
 * Represents a waiting room where players gather before starting a game.
 * 
 * Business Rule: A lobby contains actual Player entities, not just IDs.
 * This makes sense from a business perspective - when you're in a lobby,
 * you can see the other players (their names, chips, etc).
 */
public class Lobby {
    private final LobbyId id;
    private final String name;
    private final List<Player> players;
    private final int maxPlayers;
    private PlayerId adminPlayerId;
    private boolean started;

    public Lobby(LobbyId id, String name, int maxPlayers, PlayerId adminPlayerId) {
        this.id = id;
        this.name = name;
        this.maxPlayers = maxPlayers;
        this.adminPlayerId = adminPlayerId;
        this.players = new ArrayList<>();
        this.started = false;
    }

    public static Lobby create(String name, int maxPlayers, Player adminPlayer) {
        Lobby lobby = new Lobby(LobbyId.generate(), name, maxPlayers, adminPlayer.getId());
        lobby.addPlayer(adminPlayer); // Admin auto-joins
        return lobby;
    }

    public void addPlayer(Player player) {
        if (containsPlayer(player.getId())) {
            return; // Player already in lobby
        }
        if (players.size() >= maxPlayers) {
            throw new IllegalStateException("Lobby is full");
        }
        if (started) {
            throw new IllegalStateException("Lobby already started");
        }
        players.add(player);
    }

    public void removePlayer(PlayerId playerId) {
        players.removeIf(p -> p.getId().equals(playerId));
        
        // If admin left and there are still players, assign new admin
        if (adminPlayerId.equals(playerId) && !players.isEmpty()) {
            adminPlayerId = players.get(0).getId();
        }
    }

    public boolean containsPlayer(PlayerId playerId) {
        return players.stream().anyMatch(p -> p.getId().equals(playerId));
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
    public List<Player> getPlayers() { return List.copyOf(players); }
    public int getMaxPlayers() { return maxPlayers; }
    public boolean isStarted() { return started; }
    public PlayerId getAdminPlayerId() { return adminPlayerId; }
}

package com.poker.lobby.domain.events;

import java.util.List;

/**
 * Data class for PlayerLeftLobbyEvent.
 * Contains information about a player leaving a lobby.
 */
public class PlayerLeftLobbyEventData {
    private final String lobbyId;
    private final String playerId;
    private final int currentPlayerCount;
    private final String adminPlayerId;
    private final int maxPlayers;
    private final List<PlayerData> players;

    public PlayerLeftLobbyEventData(String lobbyId, String playerId, int currentPlayerCount,
                                    String adminPlayerId, int maxPlayers, List<PlayerData> players) {
        this.lobbyId = lobbyId;
        this.playerId = playerId;
        this.currentPlayerCount = currentPlayerCount;
        this.adminPlayerId = adminPlayerId;
        this.maxPlayers = maxPlayers;
        this.players = players;
    }

    public String getLobbyId() {
        return lobbyId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public int getCurrentPlayerCount() {
        return currentPlayerCount;
    }

    public String getAdminPlayerId() {
        return adminPlayerId;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public List<PlayerData> getPlayers() {
        return players;
    }
}

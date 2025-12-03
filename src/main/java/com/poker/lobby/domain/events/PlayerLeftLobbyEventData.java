package com.poker.lobby.domain.events;

/**
 * Data class for PlayerLeftLobbyEvent.
 * Contains information about a player leaving a lobby.
 */
public class PlayerLeftLobbyEventData {
    private final String lobbyId;
    private final String playerId;
    private final String playerName;
    private final int currentPlayerCount;
    private final int maxPlayers;

    public PlayerLeftLobbyEventData(String lobbyId, String playerId, String playerName,
                                    int currentPlayerCount, int maxPlayers) {
        this.lobbyId = lobbyId;
        this.playerId = playerId;
        this.playerName = playerName;
        this.currentPlayerCount = currentPlayerCount;
        this.maxPlayers = maxPlayers;
    }

    public String getLobbyId() {
        return lobbyId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getCurrentPlayerCount() {
        return currentPlayerCount;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }
}

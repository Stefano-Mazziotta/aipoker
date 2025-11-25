package com.poker.lobby.application.dto;

/**
 * Data Transfer Object for lobby join events.
 * Used by WebSocket infrastructure to serialize event data.
 */
public record PlayerJoinedLobbyDTO(
    String lobbyId,
    String playerId,
    String playerName,
    int currentPlayerCount,
    int maxPlayers
) {
    public static PlayerJoinedLobbyDTO fromEvent(String lobbyId, String playerId, 
                                                 String playerName, int currentPlayerCount, 
                                                 int maxPlayers) {
        return new PlayerJoinedLobbyDTO(lobbyId, playerId, playerName, 
                                       currentPlayerCount, maxPlayers);
    }
}

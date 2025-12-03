package com.poker.lobby.application.dto;

/**
 * Data Transfer Object for Player Left Lobby event.
 * Used to transfer player left lobby information.
 */
public record PlayerLeftLobbyDTO(
    String lobbyId,
    String playerId,
    String playerName,
    int currentPlayerCount,
    int maxPlayers
) {
    public static PlayerLeftLobbyDTO fromDomain(String lobbyId, String playerId, String playerName, 
                                                 int currentPlayerCount, int maxPlayers) {
        return new PlayerLeftLobbyDTO(lobbyId, playerId, playerName, currentPlayerCount, maxPlayers);
    }
}

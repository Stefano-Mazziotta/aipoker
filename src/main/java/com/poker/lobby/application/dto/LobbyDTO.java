package com.poker.lobby.application.dto;

/**
 * Data Transfer Object for Lobby information.
 * Used to transfer lobby data between layers without exposing domain entities.
 */
public record LobbyDTO(
    String lobbyId,
    String name,
    int currentPlayers,
    int maxPlayers,
    boolean isOpen,
    String adminPlayerId
) {
    public static LobbyDTO fromDomain(String lobbyId, String name, int currentPlayers, 
                                     int maxPlayers, boolean isOpen, String adminPlayerId) {
        return new LobbyDTO(lobbyId, name, currentPlayers, maxPlayers, isOpen, adminPlayerId);
    }
}

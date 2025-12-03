package com.poker.lobby.application.dto;

import java.util.List;

/**
 * Data Transfer Object for Lobby information.
 * Used to transfer lobby data between layers without exposing domain entities.
 */
public record LobbyDTO(
    String lobbyId,
    String lobbyName,
    int currentPlayers,
    int maxPlayers,
    boolean isOpen,
    String adminPlayerId,
    List<PlayerDTO> players
) {
    public static LobbyDTO fromDomain(
        String lobbyId,
        String lobbyName,
        int currentPlayers,
        int maxPlayers,
        boolean isOpen, 
        String adminPlayerId,
        List<PlayerDTO> players
    ) {
        return new LobbyDTO(
            lobbyId,
            lobbyName, 
            currentPlayers, 
            maxPlayers, 
            isOpen, 
            adminPlayerId, 
            players
        );
    }
}

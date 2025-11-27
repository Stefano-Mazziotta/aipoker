package com.poker.lobby.application.dto;

import java.util.List;

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
    String adminPlayerId,
    List<PlayerInLobbyDTO> players
) {
    public static LobbyDTO fromDomain(String lobbyId, String name, int currentPlayers, 
                                     int maxPlayers, boolean isOpen, String adminPlayerId,
                                     List<PlayerInLobbyDTO> players) {
        return new LobbyDTO(lobbyId, name, currentPlayers, maxPlayers, isOpen, adminPlayerId, players);
    }
    
    public record PlayerInLobbyDTO(String playerId, String playerName) {}
}

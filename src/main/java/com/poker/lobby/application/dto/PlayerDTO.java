package com.poker.lobby.application.dto;

/**
 * Data Transfer Object for Player information in lobby context.
 * Used to transfer player data without exposing domain entities.
 */
public record PlayerDTO(
    String playerId,
    String playerName,
    int chips
) {
    public static PlayerDTO fromDomain(String playerId, String playerName, int chips) {
        return new PlayerDTO(playerId, playerName, chips);
    }
}

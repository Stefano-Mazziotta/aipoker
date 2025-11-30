package com.poker.player.application.dto;

/**
 * Data Transfer Object for Player registration response.
 * Used to return player data after registration.
 */
public record RegisterPlayerDTO(
    String playerId,
    String playerName,
    int chips
) {
    public static RegisterPlayerDTO fromDomain(String playerId, String playerName, int chips) {
        return new RegisterPlayerDTO(playerId, playerName, chips);
    }
}

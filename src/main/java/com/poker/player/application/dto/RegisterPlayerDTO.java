package com.poker.player.application.dto;

/**
 * Data Transfer Object for Player registration response.
 * Used to return player data after registration.
 */
public record RegisterPlayerDTO(
    String playerId,
    String name,
    int chips
) {
    public static RegisterPlayerDTO fromDomain(String playerId, String name, int chips) {
        return new RegisterPlayerDTO(playerId, name, chips);
    }
}

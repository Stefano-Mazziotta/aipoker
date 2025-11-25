package com.poker.player.application.dto;

/**
 * Data Transfer Object for Player registration response.
 * Used to return player data after registration.
 * 
 * Note: Using 'id' instead of 'playerId' for consistency with test expectations
 * and to keep the API simple and predictable.
 */
public record RegisterPlayerDTO(
    String id,
    String name,
    int chips
) {
    public static RegisterPlayerDTO fromDomain(String playerId, String name, int chips) {
        return new RegisterPlayerDTO(playerId, name, chips);
    }
}

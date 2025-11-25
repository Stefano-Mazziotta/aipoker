package com.poker.game.application.dto;

/**
 * Data Transfer Object for Player information within a game.
 * Used to transfer player game state without exposing domain entities.
 */
public record PlayerInGameDTO(
    String playerId,
    String playerName,
    int chips,
    int currentBet,
    boolean isActive,
    boolean hasFolded,
    String position
) {
    public static PlayerInGameDTO fromDomain(String playerId, String playerName, int chips,
                                            int currentBet, boolean isActive, boolean hasFolded,
                                            String position) {
        return new PlayerInGameDTO(playerId, playerName, chips, currentBet, 
                                   isActive, hasFolded, position);
    }
}

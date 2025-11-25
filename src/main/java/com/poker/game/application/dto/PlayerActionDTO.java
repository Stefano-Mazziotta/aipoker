package com.poker.game.application.dto;

/**
 * Data Transfer Object for Player Action response.
 * Used to return action result data after a player performs an action.
 */
public record PlayerActionDTO(
    String gameState,
    int currentBet,
    int pot,
    boolean playerFolded
) {
    public static PlayerActionDTO fromDomain(String gameState, int currentBet, int pot, boolean playerFolded) {
        return new PlayerActionDTO(gameState, currentBet, pot, playerFolded);
    }
}

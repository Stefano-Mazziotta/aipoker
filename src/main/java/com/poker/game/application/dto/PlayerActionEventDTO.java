package com.poker.game.application.dto;

/**
 * Data Transfer Object for Player Action event.
 * Used to transfer player action event information.
 */
public record PlayerActionEventDTO(
    String gameId,
    String playerId,
    String playerName,
    String action,
    int amount,
    int newPot,
    int currentBet
) {
    public static PlayerActionEventDTO fromDomain(String gameId, String playerId, String playerName, 
                                                   String action, int amount, int newPot, int currentBet) {
        return new PlayerActionEventDTO(gameId, playerId, playerName, action, amount, newPot, currentBet);
    }
}

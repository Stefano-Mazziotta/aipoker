package com.poker.game.application.dto;

/**
 * Data Transfer Object for Game State Changed event.
 * Used to transfer game state changed event information.
 */
public record GameStateChangedEventDTO(
    String gameId,
    String newState,
    String currentPlayerId,
    String currentPlayerName,
    int pot
) {
    public static GameStateChangedEventDTO fromDomain(String gameId, String newState, 
                                                       String currentPlayerId, String currentPlayerName, int pot) {
        return new GameStateChangedEventDTO(gameId, newState, currentPlayerId, currentPlayerName, pot);
    }
}

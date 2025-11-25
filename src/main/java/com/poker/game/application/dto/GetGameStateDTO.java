package com.poker.game.application.dto;

/**
 * Data Transfer Object for Get Game State response.
 * Used to return current game state information including community cards and players.
 */
public record GetGameStateDTO(
    String state,
    String communityCards,
    int communityCardCount,
    int pot,
    String players,
    int playerCount
) {
    public static GetGameStateDTO fromDomain(String state, String communityCards, int communityCardCount, 
                                             int pot, String players, int playerCount) {
        return new GetGameStateDTO(state, communityCards, communityCardCount, pot, players, playerCount);
    }
}

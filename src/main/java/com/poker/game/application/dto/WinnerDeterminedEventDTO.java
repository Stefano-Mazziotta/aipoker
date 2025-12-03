package com.poker.game.application.dto;

/**
 * Data Transfer Object for Winner Determined event.
 * Used to transfer winner determination information.
 */
public record WinnerDeterminedEventDTO(
    String gameId,
    String winnerId,
    String winnerName,
    String handRank,
    int amountWon
) {
    public static WinnerDeterminedEventDTO fromDomain(String gameId, String winnerId, String winnerName, 
                                                       String handRank, int amountWon) {
        return new WinnerDeterminedEventDTO(gameId, winnerId, winnerName, handRank, amountWon);
    }
}

package com.poker.game.application.dto;

/**
 * Data Transfer Object for Determine Winner response.
 * Used to return winner information after determining the game winner.
 */
public record DetermineWinnerDTO(
    String winnerId,
    String winnerName,
    int totalChips,
    int potWon
) {
    public static DetermineWinnerDTO fromDomain(String winnerId, String winnerName, int totalChips, int potWon) {
        return new DetermineWinnerDTO(winnerId, winnerName, totalChips, potWon);
    }
}

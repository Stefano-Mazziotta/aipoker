package com.poker.game.domain.events;

/**
 * Data class for WinnerDeterminedEvent.
 * Contains information about the winner of a game.
 */
public class WinnerDeterminedEventData {
    private final String gameId;
    private final String winnerId;
    private final String winnerName;
    private final String handRank;
    private final int amountWon;

    public WinnerDeterminedEventData(String gameId, String winnerId, String winnerName,
                                     String handRank, int amountWon) {
        this.gameId = gameId;
        this.winnerId = winnerId;
        this.winnerName = winnerName;
        this.handRank = handRank;
        this.amountWon = amountWon;
    }

    public String getGameId() {
        return gameId;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public String getHandRank() {
        return handRank;
    }

    public int getAmountWon() {
        return amountWon;
    }
}

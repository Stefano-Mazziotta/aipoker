package com.poker.shared.infrastructure.events;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Event fired when game ends and winner is determined.
 */
public class WinnerDeterminedEvent extends GameEvent {
    private static final Gson gson = new GsonBuilder().create();
    
    private final String winnerId;
    private final String winnerName;
    private final String handRank;
    private final int amountWon;

    public WinnerDeterminedEvent(String gameId, String winnerId, String winnerName, 
                                 String handRank, int amountWon) {
        super(gameId, "WINNER_DETERMINED");
        this.winnerId = winnerId;
        this.winnerName = winnerName;
        this.handRank = handRank;
        this.amountWon = amountWon;
    }

    @Override
    public String toJson() {
        return gson.toJson(this);
    }

    public String getWinnerId() { return winnerId; }
    public String getWinnerName() { return winnerName; }
    public String getHandRank() { return handRank; }
    public int getAmountWon() { return amountWon; }
}

package com.poker.game.domain.events;

import com.poker.shared.domain.events.DomainEvent;

/**
 * Domain event fired when game ends and winner is determined.
 */
public class WinnerDeterminedEvent extends DomainEvent {
    private final String gameId;
    private final String winnerId;
    private final String winnerName;
    private final String handRank;
    private final int amountWon;

    public WinnerDeterminedEvent(String gameId, String winnerId, String winnerName, 
                                 String handRank, int amountWon) {
        super();
        this.gameId = gameId;
        this.winnerId = winnerId;
        this.winnerName = winnerName;
        this.handRank = handRank;
        this.amountWon = amountWon;
    }

    @Override
    public String eventType() {
        return "WINNER_DETERMINED";
    }

    public String gameId() { return gameId; }
    public String winnerId() { return winnerId; }
    public String winnerName() { return winnerName; }
    public String handRank() { return handRank; }
    public int amountWon() { return amountWon; }
}

package com.poker.game.domain.events;

/**
 * Data class for RoundCompletedEvent.
 * Contains information about the completed betting round.
 */
public class RoundCompletedEventData {
    private final String gameId;
    private final String completedPhase;
    private final String nextPhase;

    public RoundCompletedEventData(String gameId, String completedPhase, String nextPhase) {
        this.gameId = gameId;
        this.completedPhase = completedPhase;
        this.nextPhase = nextPhase;
    }

    public String getGameId() {
        return gameId;
    }

    public String getCompletedPhase() {
        return completedPhase;
    }

    public String getNextPhase() {
        return nextPhase;
    }
}

package com.poker.game.domain.events;

import java.util.List;

/**
 * Data class for DealtCardsEvent.
 * Contains information about cards dealt in a game.
 */
public class DealtCardsEventData {
    private final String gameId;
    private final String phase;
    private final List<String> newCards;
    private final List<String> allCommunityCards;

    public DealtCardsEventData(String gameId, String phase, List<String> newCards,
                               List<String> allCommunityCards) {
        this.gameId = gameId;
        this.phase = phase;
        this.newCards = List.copyOf(newCards);
        this.allCommunityCards = List.copyOf(allCommunityCards);
    }

    public String getGameId() {
        return gameId;
    }

    public String getPhase() {
        return phase;
    }

    public List<String> getNewCards() {
        return newCards;
    }

    public List<String> getAllCommunityCards() {
        return allCommunityCards;
    }
}

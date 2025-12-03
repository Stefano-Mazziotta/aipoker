package com.poker.game.domain.events;

import java.util.List;

/**
 * Data class for GameStateChangedEvent.
 * Contains information about a game state change.
 */
public class GameStateChangedEventData {
    private final String gameId;
    private final String newState;
    private final String currentPlayerId;
    private final String currentPlayerName;
    private final int pot;
    private final int currentBet;
    private final List<String> communityCards;

    public GameStateChangedEventData(String gameId, String newState, String currentPlayerId,
                                     String currentPlayerName, int pot, int currentBet, List<String> communityCards) {
        this.gameId = gameId;
        this.newState = newState;
        this.currentPlayerId = currentPlayerId;
        this.currentPlayerName = currentPlayerName;
        this.pot = pot;
        this.currentBet = currentBet;
        this.communityCards = communityCards;
    }

    public String getGameId() {
        return gameId;
    }

    public String getNewState() {
        return newState;
    }

    public String getCurrentPlayerId() {
        return currentPlayerId;
    }

    public String getCurrentPlayerName() {
        return currentPlayerName;
    }

    public int getPot() {
        return pot;
    }

    public int getCurrentBet() {
        return currentBet;
    }

    public List<String> getCommunityCards() {
        return communityCards;
    }
}

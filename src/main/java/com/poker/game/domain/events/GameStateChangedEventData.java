package com.poker.game.domain.events;

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

    public GameStateChangedEventData(String gameId, String newState, String currentPlayerId,
                                     String currentPlayerName, int pot) {
        this.gameId = gameId;
        this.newState = newState;
        this.currentPlayerId = currentPlayerId;
        this.currentPlayerName = currentPlayerName;
        this.pot = pot;
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
}

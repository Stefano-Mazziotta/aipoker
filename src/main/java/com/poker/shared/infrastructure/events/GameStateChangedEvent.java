package com.poker.shared.infrastructure.events;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Event fired when game state changes (phase transitions, turn changes, etc).
 */
public class GameStateChangedEvent extends GameEvent {
    private static final Gson gson = new GsonBuilder().create();
    
    private final String newState;
    private final String currentPlayerId;
    private final String currentPlayerName;
    private final int pot;

    public GameStateChangedEvent(String gameId, String newState, String currentPlayerId, 
                                 String currentPlayerName, int pot) {
        super(gameId, "GAME_STATE_CHANGED");
        this.newState = newState;
        this.currentPlayerId = currentPlayerId;
        this.currentPlayerName = currentPlayerName;
        this.pot = pot;
    }

    @Override
    public String toJson() {
        return gson.toJson(this);
    }

    public String getNewState() { return newState; }
    public String getCurrentPlayerId() { return currentPlayerId; }
    public String getCurrentPlayerName() { return currentPlayerName; }
    public int getPot() { return pot; }
}

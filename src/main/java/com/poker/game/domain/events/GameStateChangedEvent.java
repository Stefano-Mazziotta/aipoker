package com.poker.game.domain.events;

import com.poker.shared.domain.events.DomainEvent;

/**
 * Domain event fired when the game state changes (phase transitions, turn changes, etc).
 */
public class GameStateChangedEvent extends DomainEvent {
    private final String gameId;
    private final String newState;
    private final String currentPlayerId;
    private final String currentPlayerName;
    private final int pot;

    public GameStateChangedEvent(String gameId, String newState, String currentPlayerId, 
                                 String currentPlayerName, int pot) {
        super();
        this.gameId = gameId;
        this.newState = newState;
        this.currentPlayerId = currentPlayerId;
        this.currentPlayerName = currentPlayerName;
        this.pot = pot;
    }

    @Override
    public String eventType() {
        return "GAME_STATE_CHANGED";
    }

    public String gameId() { return gameId; }
    public String newState() { return newState; }
    public String currentPlayerId() { return currentPlayerId; }
    public String currentPlayerName() { return currentPlayerName; }
    public int pot() { return pot; }
}

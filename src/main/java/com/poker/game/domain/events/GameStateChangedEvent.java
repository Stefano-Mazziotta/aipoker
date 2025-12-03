package com.poker.game.domain.events;

import com.poker.shared.domain.enums.EventTypeEnum;
import com.poker.shared.domain.events.DomainEvent;

/**
 * Domain event fired when the game state changes (phase transitions, turn changes, etc).
 */
public class GameStateChangedEvent extends DomainEvent {
    private final GameStateChangedEventData data;

    public GameStateChangedEvent(
        String gameId,
        String newState, 
        String currentPlayerId,
        String currentPlayerName,
        int pot
    ) {
        super(EventTypeEnum.GAME_STATE_CHANGED);
        this.data = new GameStateChangedEventData(gameId, newState, currentPlayerId, currentPlayerName, pot);
    }

    @Override
    public EventTypeEnum eventType() {
        return EventTypeEnum.GAME_STATE_CHANGED;
    }

    @Override
    public GameStateChangedEventData getData() {
        return data;
    }
}

package com.poker.game.domain.events;

import com.poker.shared.domain.enums.EventTypeEnum;
import com.poker.shared.domain.events.DomainEvent;

/**
 * Domain event fired when a player performs an action (FOLD, CHECK, CALL, RAISE, ALL_IN).
 */
public class PlayerActionEvent extends DomainEvent {
    private final PlayerActionEventData data;

    public PlayerActionEvent(
        String gameId, 
        String playerId, 
        String playerName,
        String action, 
        int amount, 
        int newPot, 
        int currentBet
    ) {
        super(EventTypeEnum.PLAYER_ACTION);
        this.data = new PlayerActionEventData(gameId, playerId, playerName, action, amount, newPot, currentBet);
    }

    @Override
    public EventTypeEnum eventType() {
        return EventTypeEnum.PLAYER_ACTION;
    }

    @Override
    public PlayerActionEventData getData() {
        return data;
    }
}

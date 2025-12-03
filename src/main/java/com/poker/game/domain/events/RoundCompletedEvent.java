package com.poker.game.domain.events;

import com.poker.shared.domain.enums.EventTypeEnum;
import com.poker.shared.domain.events.DomainEvent;

/**
 * Domain event fired when a betting round is completed.
 * Signals that all players have acted and the game can progress to the next phase.
 */
public class RoundCompletedEvent extends DomainEvent {
    private final RoundCompletedEventData data;

    public RoundCompletedEvent(String gameId, String completedPhase, String nextPhase) {
        super(EventTypeEnum.ROUND_COMPLETED);
        this.data = new RoundCompletedEventData(gameId, completedPhase, nextPhase);
    }

    @Override
    public EventTypeEnum eventType() {
        return EventTypeEnum.ROUND_COMPLETED;
    }

    @Override
    public RoundCompletedEventData getData() {
        return data;
    }
}

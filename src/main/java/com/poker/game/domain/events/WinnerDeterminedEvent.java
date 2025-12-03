package com.poker.game.domain.events;

import com.poker.shared.domain.enums.EventTypeEnum;
import com.poker.shared.domain.events.DomainEvent;

/**
 * Domain event fired when game ends and winner is determined.
 */
public class WinnerDeterminedEvent extends DomainEvent {
    private final WinnerDeterminedEventData data;

    public WinnerDeterminedEvent(
        String gameId, 
        String winnerId, 
        String winnerName,
        String handRank, 
        int amountWon
    ) {
        super(EventTypeEnum.WINNER_DETERMINED);
        this.data = new WinnerDeterminedEventData(gameId, winnerId, winnerName, handRank, amountWon);
    }

    @Override
    public EventTypeEnum eventType() {
        return EventTypeEnum.WINNER_DETERMINED;
    }

    @Override
    public WinnerDeterminedEventData getData() {
        return data;
    }
}

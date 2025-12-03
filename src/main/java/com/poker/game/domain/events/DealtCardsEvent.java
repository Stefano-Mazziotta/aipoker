package com.poker.game.domain.events;

import java.util.List;

import com.poker.shared.domain.enums.EventTypeEnum;
import com.poker.shared.domain.events.DomainEvent;

/**
 * Domain event fired when community cards are dealt (FLOP, TURN, RIVER).
 */
public class DealtCardsEvent extends DomainEvent {
    private final DealtCardsEventData data;

    public DealtCardsEvent(String gameId, String phase, List<String> newCards, List<String> allCommunityCards) {
        super();
        this.data = new DealtCardsEventData(gameId, phase, newCards, allCommunityCards);
    }

    @Override
    public EventTypeEnum eventType() {
        return EventTypeEnum.DEALT_CARDS;
    }

    @Override
    public DealtCardsEventData getData() {
        return data;
    }
}

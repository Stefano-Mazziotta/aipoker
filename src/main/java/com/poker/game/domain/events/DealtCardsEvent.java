package com.poker.game.domain.events;

import java.util.List;

import com.poker.shared.domain.enums.EventTypeEnum;
import com.poker.shared.domain.events.DomainEvent;

/**
 * Domain event fired when community cards are dealt (FLOP, TURN, RIVER).
 */
public class DealtCardsEvent extends DomainEvent {
    private final String gameId;
    private final String phase;
    private final List<String> newCards;
    private final List<String> allCommunityCards;

    public DealtCardsEvent(String gameId, String phase, List<String> newCards, List<String> allCommunityCards) {
        super();
        this.gameId = gameId;
        this.phase = phase;
        this.newCards = List.copyOf(newCards);
        this.allCommunityCards = List.copyOf(allCommunityCards);
    }

    @Override
    public EventTypeEnum eventType() {
        return EventTypeEnum.DEALT_CARDS;
    }

    @Override
    public Object getData() {
        return this;
    }

    public String gameId() { return gameId; }
    public String phase() { return phase; }
    public List<String> newCards() { return newCards; }
    public List<String> allCommunityCards() { return allCommunityCards; }
}

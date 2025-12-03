package com.poker.game.domain.events;

import java.util.List;

import com.poker.shared.domain.enums.EventTypeEnum;
import com.poker.shared.domain.events.DomainEvent;

/**
 * Domain event fired when a player's hole cards are dealt.
 * This event is sent individually to each player to provide their private cards.
 */
public class PlayerCardsDealtEvent extends DomainEvent {
    private final PlayerCardsDealtEventData data;

    public PlayerCardsDealtEvent(String gameId, String playerId, List<String> cards) {
        super(EventTypeEnum.PLAYER_CARDS_DEALT);
        this.data = new PlayerCardsDealtEventData(gameId, playerId, cards);
    }

    @Override
    public EventTypeEnum eventType() {
        return EventTypeEnum.PLAYER_CARDS_DEALT;
    }

    @Override
    public PlayerCardsDealtEventData getData() {
        return data;
    }
}

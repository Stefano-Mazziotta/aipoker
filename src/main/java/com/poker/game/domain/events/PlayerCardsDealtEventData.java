package com.poker.game.domain.events;

import java.util.List;

/**
 * Data class for PlayerCardsDealtEvent.
 * Contains information about a player's hole cards.
 */
public class PlayerCardsDealtEventData {
    private final String gameId;
    private final String playerId;
    private final List<String> cards;

    public PlayerCardsDealtEventData(String gameId, String playerId, List<String> cards) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.cards = List.copyOf(cards);
    }

    public String getGameId() {
        return gameId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public List<String> getCards() {
        return cards;
    }
}

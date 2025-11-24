package com.poker.shared.infrastructure.events;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Event fired when community cards are dealt (FLOP, TURN, RIVER).
 */
public class CardsDealtEvent extends GameEvent {
    private static final Gson gson = new GsonBuilder().create();
    
    private final String phase;
    private final List<String> newCards;
    private final List<String> allCommunityCards;

    public CardsDealtEvent(String gameId, String phase, List<String> newCards, List<String> allCommunityCards) {
        super(gameId, "CARDS_DEALT");
        this.phase = phase;
        this.newCards = newCards;
        this.allCommunityCards = allCommunityCards;
    }

    @Override
    public String toJson() {
        return gson.toJson(this);
    }

    public String getPhase() { return phase; }
    public List<String> getNewCards() { return newCards; }
    public List<String> getAllCommunityCards() { return allCommunityCards; }
}

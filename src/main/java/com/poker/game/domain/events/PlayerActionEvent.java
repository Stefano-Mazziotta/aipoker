package com.poker.game.domain.events;

import com.poker.shared.domain.enums.EventTypeEnum;
import com.poker.shared.domain.events.DomainEvent;

/**
 * Domain event fired when a player performs an action (FOLD, CHECK, CALL, RAISE, ALL_IN).
 */
public class PlayerActionEvent extends DomainEvent {
    private final String gameId;
    private final String playerId;
    private final String playerName;
    private final String action;
    private final int amount;
    private final int newPot;
    private final int currentBet;

    public PlayerActionEvent(
        String gameId, 
        String playerId, 
        String playerName,
        String action, 
        int amount, 
        int newPot, 
        int currentBet
    ) {
        super();
        this.gameId = gameId;
        this.playerId = playerId;
        this.playerName = playerName;
        this.action = action;
        this.amount = amount;
        this.newPot = newPot;
        this.currentBet = currentBet;
    }

    @Override
    public EventTypeEnum eventType() {
        return EventTypeEnum.PLAYER_ACTION;
    }

    @Override
    public Object getData() {
        return this;
    }

    public String gameId() { return gameId; }
    public String playerId() { return playerId; }
    public String playerName() { return playerName; }
    public String action() { return action; }
    public int amount() { return amount; }
    public int newPot() { return newPot; }
    public int currentBet() { return currentBet; }
}

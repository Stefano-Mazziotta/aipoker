package com.poker.shared.infrastructure.events;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Event fired when a player performs an action (FOLD, CHECK, CALL, RAISE, ALL_IN).
 */
public class PlayerActionEvent extends GameEvent {
    private static final Gson gson = new GsonBuilder().create();
    
    private final String playerId;
    private final String playerName;
    private final String action;
    private final int amount;
    private final int newPot;
    private final int currentBet;

    public PlayerActionEvent(String gameId, String playerId, String playerName, 
                            String action, int amount, int newPot, int currentBet) {
        super(gameId, "PLAYER_ACTION");
        this.playerId = playerId;
        this.playerName = playerName;
        this.action = action;
        this.amount = amount;
        this.newPot = newPot;
        this.currentBet = currentBet;
    }

    @Override
    public String toJson() {
        return gson.toJson(this);
    }

    // Getters
    public String getPlayerId() { return playerId; }
    public String getPlayerName() { return playerName; }
    public String getAction() { return action; }
    public int getAmount() { return amount; }
    public int getNewPot() { return newPot; }
    public int getCurrentBet() { return currentBet; }
}

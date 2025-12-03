package com.poker.game.domain.events;

/**
 * Data class for PlayerActionEvent.
 * Contains information about a player's action in a game.
 */
public class PlayerActionEventData {
    private final String gameId;
    private final String playerId;
    private final String playerName;
    private final String action;
    private final int amount;
    private final int newPot;
    private final int currentBet;

    public PlayerActionEventData(String gameId, String playerId, String playerName,
                                 String action, int amount, int newPot, int currentBet) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.playerName = playerName;
        this.action = action;
        this.amount = amount;
        this.newPot = newPot;
        this.currentBet = currentBet;
    }

    public String getGameId() {
        return gameId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getAction() {
        return action;
    }

    public int getAmount() {
        return amount;
    }

    public int getNewPot() {
        return newPot;
    }

    public int getCurrentBet() {
        return currentBet;
    }
}

package com.poker.game.domain.events;

/**
 * Data class for player information in game events.
 * Contains detailed player state information needed for game UI.
 */
public class GamePlayerData {
    private final String playerId;
    private final String playerName;
    private final int chips;
    private final int currentBet;
    private final boolean isFolded;
    private final boolean isAllIn;

    public GamePlayerData(
        String playerId, 
        String playerName, 
        int chips,
        int currentBet,
        boolean isFolded,
        boolean isAllIn
    ) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.chips = chips;
        this.currentBet = currentBet;
        this.isFolded = isFolded;
        this.isAllIn = isAllIn;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getChips() {
        return chips;
    }

    public int getCurrentBet() {
        return currentBet;
    }

    public boolean isFolded() {
        return isFolded;
    }

    public boolean isAllIn() {
        return isAllIn;
    }
}

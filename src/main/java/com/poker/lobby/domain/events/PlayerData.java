package com.poker.lobby.domain.events;

/**
 * Data class for player information in lobby events.
 * Shared between PlayerJoinedLobbyEvent and PlayerLeftLobbyEvent.
 */
public class PlayerData {
    private final String playerId;
    private final String playerName;
    private final int chips;

    public PlayerData(String playerId, String playerName, int chips) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.chips = chips;
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
}

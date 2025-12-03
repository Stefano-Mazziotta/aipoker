package com.poker.lobby.domain.events;

import java.util.List;

/**
 * Data class for PlayerJoinedLobbyEvent.
 * Contains lobby information when a player joins.
 */
public class PlayerJoinedLobbyEventData {
    private final String lobbyId;
    private final String lobbyName;
    private final int currentPlayers;
    private final int maxPlayers;
    private final boolean isOpen;
    private final String adminPlayerId;
    private final List<PlayerData> players;

    public PlayerJoinedLobbyEventData(String lobbyId, String lobbyName, int currentPlayers,
                                      int maxPlayers, boolean isOpen, String adminPlayerId,
                                      List<PlayerData> players) {
        this.lobbyId = lobbyId;
        this.lobbyName = lobbyName;
        this.currentPlayers = currentPlayers;
        this.maxPlayers = maxPlayers;
        this.isOpen = isOpen;
        this.adminPlayerId = adminPlayerId;
        this.players = players;
    }

    public String getLobbyId() {
        return lobbyId;
    }

    public String getLobbyName() {
        return lobbyName;
    }

    public int getCurrentPlayers() {
        return currentPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public String getAdminPlayerId() {
        return adminPlayerId;
    }

    public List<PlayerData> getPlayers() {
        return players;
    }

    /**
     * Nested data class for player information.
     */
    public static class PlayerData {
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
}

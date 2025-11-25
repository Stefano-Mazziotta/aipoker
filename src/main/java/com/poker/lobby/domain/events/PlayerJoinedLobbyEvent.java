package com.poker.lobby.domain.events;

import com.poker.shared.domain.events.DomainEvent;

/**
 * Domain event fired when a player joins a lobby.
 */
public class PlayerJoinedLobbyEvent extends DomainEvent {
    private final String lobbyId;
    private final String playerId;
    private final String playerName;
    private final int currentPlayerCount;
    private final int maxPlayers;

    public PlayerJoinedLobbyEvent(String lobbyId, String playerId, String playerName,
                                  int currentPlayerCount, int maxPlayers) {
        super();
        this.lobbyId = lobbyId;
        this.playerId = playerId;
        this.playerName = playerName;
        this.currentPlayerCount = currentPlayerCount;
        this.maxPlayers = maxPlayers;
    }

    @Override
    public String eventType() {
        return "PLAYER_JOINED_LOBBY";
    }

    public String lobbyId() { return lobbyId; }
    public String playerId() { return playerId; }
    public String playerName() { return playerName; }
    public int currentPlayerCount() { return currentPlayerCount; }
    public int maxPlayers() { return maxPlayers; }
}

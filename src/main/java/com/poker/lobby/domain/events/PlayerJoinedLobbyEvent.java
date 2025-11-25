package com.poker.lobby.domain.events;

import com.poker.shared.domain.events.DomainEvent;

/**
 * Domain event fired when a player joins a lobby.
 */
public class PlayerJoinedLobbyEvent extends DomainEvent {
    private final String lobbyId;
    private final String playerId;
    private final int currentPlayerCount;
    private final int maxPlayers;

    public PlayerJoinedLobbyEvent(String lobbyId, String playerId, 
                                  int currentPlayerCount, int maxPlayers) {
        super();
        this.lobbyId = lobbyId;
        this.playerId = playerId;
        this.currentPlayerCount = currentPlayerCount;
        this.maxPlayers = maxPlayers;
    }

    @Override
    public String eventType() {
        return "PLAYER_JOINED_LOBBY";
    }

    public String lobbyId() { return lobbyId; }
    public String playerId() { return playerId; }
    public int currentPlayerCount() { return currentPlayerCount; }
    public int maxPlayers() { return maxPlayers; }
}

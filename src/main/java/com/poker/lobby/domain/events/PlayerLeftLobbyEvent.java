package com.poker.lobby.domain.events;

import com.poker.shared.domain.enums.EventTypeEnum;
import com.poker.shared.domain.events.DomainEvent;

/**
 * Domain event fired when a player leaves a lobby.
 */
public class PlayerLeftLobbyEvent extends DomainEvent {
    private final String lobbyId;
    private final String playerId;
    private final String playerName;
    private final int currentPlayerCount;
    private final int maxPlayers;

    public PlayerLeftLobbyEvent(
        String lobbyId, 
        String playerId, 
        String playerName,
        int currentPlayerCount, 
        int maxPlayers
    ) {
        super();
        this.lobbyId = lobbyId;
        this.playerId = playerId;
        this.playerName = playerName;
        this.currentPlayerCount = currentPlayerCount;
        this.maxPlayers = maxPlayers;
    }

    @Override
    public EventTypeEnum eventType() {
        return EventTypeEnum.PLAYER_LEFT_LOBBY;
    }

    @Override
    public Object getData() {
        return this;
    }

    public String lobbyId() { return lobbyId; }
    public String playerId() { return playerId; }
    public String playerName() { return playerName; }
    public int currentPlayerCount() { return currentPlayerCount; }
    public int maxPlayers() { return maxPlayers; }
}

package com.poker.lobby.domain.events;

import com.poker.shared.domain.enums.EventTypeEnum;
import com.poker.shared.domain.events.DomainEvent;

/**
 * Domain event fired when a player leaves a lobby.
 */
public class PlayerLeftLobbyEvent extends DomainEvent {
    private final PlayerLeftLobbyEventData data;

    public PlayerLeftLobbyEvent(
        String lobbyId, 
        String playerId, 
        String playerName,
        int currentPlayerCount, 
        int maxPlayers
    ) {
        super(EventTypeEnum.PLAYER_LEFT_LOBBY);
        this.data = new PlayerLeftLobbyEventData(lobbyId, playerId, playerName, currentPlayerCount, maxPlayers);
    }

    @Override
    public EventTypeEnum eventType() {
        return EventTypeEnum.PLAYER_LEFT_LOBBY;
    }

    @Override
    public PlayerLeftLobbyEventData getData() {
        return data;
    }
}

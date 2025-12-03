package com.poker.lobby.domain.events;

import java.util.List;

import com.poker.shared.domain.enums.EventTypeEnum;
import com.poker.shared.domain.events.DomainEvent;

/**
 * Domain event fired when a player joins a lobby.
 */
public class PlayerJoinedLobbyEvent extends DomainEvent {
    private final PlayerJoinedLobbyEventData data;

    public PlayerJoinedLobbyEvent(String lobbyId, String lobbyName, int currentPlayers,
                                  int maxPlayers, boolean isOpen, String adminPlayerId,
                                  List<PlayerJoinedLobbyEventData.PlayerData> players) {
        super();
        this.data = new PlayerJoinedLobbyEventData(lobbyId, lobbyName, currentPlayers,
                                                    maxPlayers, isOpen, adminPlayerId, players);
    }

    @Override
    public EventTypeEnum eventType() {
        return EventTypeEnum.PLAYER_JOINED_LOBBY;
    }

    @Override
    public PlayerJoinedLobbyEventData getData() {
        return data;
    }
}

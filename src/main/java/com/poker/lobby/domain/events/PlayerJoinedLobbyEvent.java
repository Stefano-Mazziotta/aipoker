package com.poker.lobby.domain.events;

import com.poker.lobby.application.dto.LobbyDTO;
import com.poker.shared.domain.enums.EventTypeEnum;
import com.poker.shared.domain.events.DomainEvent;

/**
 * Domain event fired when a player joins a lobby.
 */
public class PlayerJoinedLobbyEvent extends DomainEvent {
    LobbyDTO dto;

    public PlayerJoinedLobbyEvent(LobbyDTO dto) {
        super();
        this.dto = dto;
    }

    @Override
    public EventTypeEnum eventType() {
        return EventTypeEnum.PLAYER_JOINED_LOBBY;
    }

    @Override
    public LobbyDTO getData() {
        return dto;
    }
}

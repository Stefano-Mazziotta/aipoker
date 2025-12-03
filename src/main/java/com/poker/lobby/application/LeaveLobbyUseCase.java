package com.poker.lobby.application;

import java.util.List;
import java.util.stream.Collectors;

import com.poker.lobby.domain.events.PlayerData;
import com.poker.lobby.domain.events.PlayerLeftLobbyEvent;
import com.poker.lobby.domain.model.Lobby;
import com.poker.lobby.domain.model.LobbyId;
import com.poker.lobby.domain.repository.LobbyRepository;
import com.poker.player.domain.model.PlayerId;
import com.poker.shared.domain.events.DomainEventPublisher;

/**
 * Use case for leaving a lobby.
 */
public class LeaveLobbyUseCase {
    private final LobbyRepository lobbyRepository;
    private final DomainEventPublisher eventPublisher;

    public LeaveLobbyUseCase(LobbyRepository lobbyRepository,
                            DomainEventPublisher eventPublisher) {
        this.lobbyRepository = lobbyRepository;
        this.eventPublisher = eventPublisher;
    }

    public void execute(LeaveLobbyCommand command) {
        // Load lobby
        Lobby lobby = lobbyRepository.findById(new LobbyId(command.lobbyId()))
            .orElseThrow(() -> new IllegalArgumentException("Lobby not found"));

        // Load player
        PlayerId playerId = PlayerId.from(command.playerId());
        
        // Remove player from lobby
        lobby.removePlayer(playerId);

        // unsubscribe player from lobby events
        eventPublisher.unsubscribeFromScope(lobby.getId().getValue(), playerId.getValue().toString());
        
        // Save updated lobby
        lobbyRepository.save(lobby);

        // Publish event to notify all lobby subscribers
        List<PlayerData> eventPlayers = lobby.getPlayers().stream()
            .map(p -> new PlayerData(
                p.getId().getValue().toString(),
                p.getName(),
                p.getChips().getAmount()
            ))
            .collect(Collectors.toList());

        PlayerLeftLobbyEvent event = new PlayerLeftLobbyEvent(
            lobby.getId().getValue(),
            playerId.getValue().toString(),
            lobby.getPlayers().size(),
            lobby.getAdminPlayerId().getValue().toString(),
            lobby.getMaxPlayers(),
            eventPlayers
        );
        eventPublisher.publishToScope(lobby.getId().getValue(), event);
    }

    public record LeaveLobbyCommand(String lobbyId, String playerId) {}
}

package com.poker.lobby.application;

import com.poker.lobby.domain.events.PlayerJoinedLobbyEvent;
import com.poker.lobby.domain.model.Lobby;
import com.poker.lobby.domain.model.LobbyId;
import com.poker.lobby.domain.repository.LobbyRepository;
import com.poker.player.domain.model.PlayerId;
import com.poker.shared.domain.events.DomainEventPublisher;

/**
 * Use case for joining a lobby.
 */
public class JoinLobbyUseCase {
    private final LobbyRepository lobbyRepository;
    private final DomainEventPublisher eventPublisher;

    public JoinLobbyUseCase(LobbyRepository lobbyRepository, DomainEventPublisher eventPublisher) {
        this.lobbyRepository = lobbyRepository;
        this.eventPublisher = eventPublisher;
    }

    public LobbyResponse execute(JoinLobbyCommand command) {
        // Load lobby
        Lobby lobby = lobbyRepository.findById(new LobbyId(command.lobbyId()))
            .orElseThrow(() -> new IllegalArgumentException("Lobby not found"));

        // Add player to lobby
        PlayerId playerId = PlayerId.from(command.playerId());
        lobby.addPlayer(playerId);
        
        // Save updated lobby
        lobbyRepository.save(lobby);

        // Publish event to notify all lobby subscribers
        PlayerJoinedLobbyEvent event = new PlayerJoinedLobbyEvent(
            lobby.getId().getValue(),
            playerId.getValue().toString(),
            lobby.getPlayers().size(),
            lobby.getMaxPlayers()
        );
        eventPublisher.publishToScope(lobby.getId().getValue(), event);

        return new LobbyResponse(
            lobby.getId().getValue(),
            lobby.getName(),
            lobby.getPlayers().size(),
            lobby.getMaxPlayers(),
            lobby.isOpen(),
            lobby.getAdminPlayerId().getValue().toString()
        );
    }

    public record JoinLobbyCommand(String lobbyId, String playerId) {}
    
    public record LobbyResponse(
        String lobbyId,
        String name,
        int currentPlayers,
        int maxPlayers,
        boolean isOpen,
        String adminPlayerId
    ) {}
}

package com.poker.lobby.application;

import com.poker.lobby.domain.events.PlayerLeftLobbyEvent;
import com.poker.lobby.domain.model.Lobby;
import com.poker.lobby.domain.model.LobbyId;
import com.poker.lobby.domain.repository.LobbyRepository;
import com.poker.player.domain.model.Player;
import com.poker.player.domain.model.PlayerId;
import com.poker.player.domain.repository.PlayerRepository;
import com.poker.shared.domain.events.DomainEventPublisher;

/**
 * Use case for leaving a lobby.
 */
public class LeaveLobbyUseCase {
    private final LobbyRepository lobbyRepository;
    private final PlayerRepository playerRepository;
    private final DomainEventPublisher eventPublisher;

    public LeaveLobbyUseCase(LobbyRepository lobbyRepository, PlayerRepository playerRepository,
                            DomainEventPublisher eventPublisher) {
        this.lobbyRepository = lobbyRepository;
        this.playerRepository = playerRepository;
        this.eventPublisher = eventPublisher;
    }

    public void execute(LeaveLobbyCommand command) {
        // Load lobby
        Lobby lobby = lobbyRepository.findById(new LobbyId(command.lobbyId()))
            .orElseThrow(() -> new IllegalArgumentException("Lobby not found"));

        // Load player
        PlayerId playerId = PlayerId.from(command.playerId());
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        
        // Remove player from lobby
        lobby.removePlayer(playerId);
        
        // Save updated lobby
        lobbyRepository.save(lobby);

        // Publish event to notify all lobby subscribers
        PlayerLeftLobbyEvent event = new PlayerLeftLobbyEvent(
            lobby.getId().getValue(),
            playerId.getValue().toString(),
            player.getName(),
            lobby.getPlayers().size(),
            lobby.getMaxPlayers()
        );
        eventPublisher.publishToScope(lobby.getId().getValue(), event);
    }

    public record LeaveLobbyCommand(String lobbyId, String playerId) {}
}

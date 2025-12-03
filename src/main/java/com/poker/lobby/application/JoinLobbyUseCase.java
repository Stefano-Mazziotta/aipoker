package com.poker.lobby.application;

import java.util.List;
import java.util.stream.Collectors;

import com.poker.lobby.application.dto.LobbyDTO;
import com.poker.lobby.application.dto.PlayerDTO;
import com.poker.lobby.domain.events.PlayerJoinedLobbyEvent;
import com.poker.lobby.domain.events.PlayerJoinedLobbyEventData;
import com.poker.lobby.domain.model.Lobby;
import com.poker.lobby.domain.model.LobbyId;
import com.poker.lobby.domain.repository.LobbyRepository;
import com.poker.player.domain.model.Player;
import com.poker.player.domain.model.PlayerId;
import com.poker.player.domain.repository.PlayerRepository;
import com.poker.shared.domain.events.DomainEventPublisher;

/**
 * Use case for joining a lobby.
 */
public class JoinLobbyUseCase {
    private final LobbyRepository lobbyRepository;
    private final PlayerRepository playerRepository;
    private final DomainEventPublisher eventPublisher;

    public JoinLobbyUseCase(LobbyRepository lobbyRepository, PlayerRepository playerRepository,
                           DomainEventPublisher eventPublisher) {
        this.lobbyRepository = lobbyRepository;
        this.playerRepository = playerRepository;
        this.eventPublisher = eventPublisher;
    }

    public LobbyDTO execute(JoinLobbyCommand command) {
        // Load lobby (with all players via JOIN)
        Lobby lobby = lobbyRepository.findById(new LobbyId(command.lobbyId()))
            .orElseThrow(() -> new IllegalArgumentException("Lobby not found"));

        // Load player
        PlayerId playerId = PlayerId.from(command.playerId());
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        
        // Add player to lobby
        lobby.addPlayer(player);
        
        // Save updated lobby
        lobbyRepository.save(lobby);

        // Convert players to DTOs for response
        List<PlayerDTO> players = lobby.getPlayers().stream()
            .map(p -> PlayerDTO.fromDomain(
                p.getId().getValue().toString(),
                p.getName(),
                p.getChips().getAmount()
            ))
            .collect(Collectors.toList());

        LobbyDTO dto = LobbyDTO.fromDomain(
            lobby.getId().getValue(),
            lobby.getName(),
            lobby.getPlayers().size(),
            lobby.getMaxPlayers(),
            lobby.isOpen(),
            lobby.getAdminPlayerId().getValue().toString(),
            players
        );

        // Publish domain event to notify all lobby subscribers
        List<PlayerJoinedLobbyEventData.PlayerData> eventPlayers = lobby.getPlayers().stream()
            .map(p -> new PlayerJoinedLobbyEventData.PlayerData(
                p.getId().getValue().toString(),
                p.getName(),
                p.getChips().getAmount()
            ))
            .collect(Collectors.toList());
        
        PlayerJoinedLobbyEvent event = new PlayerJoinedLobbyEvent(
            lobby.getId().getValue(),
            lobby.getName(),
            lobby.getPlayers().size(),
            lobby.getMaxPlayers(),
            lobby.isOpen(),
            lobby.getAdminPlayerId().getValue().toString(),
            eventPlayers
        );
        eventPublisher.publishToScope(lobby.getId().getValue(), event);

        return dto;
    }

    public record JoinLobbyCommand(String lobbyId, String playerId) {}
}

package com.poker.lobby.application;

import com.poker.lobby.domain.model.*;
import com.poker.lobby.domain.repository.LobbyRepository;
import com.poker.player.domain.model.PlayerId;

/**
 * Use case for joining a lobby.
 */
public class JoinLobbyUseCase {
    private final LobbyRepository lobbyRepository;

    public JoinLobbyUseCase(LobbyRepository lobbyRepository) {
        this.lobbyRepository = lobbyRepository;
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

        return new LobbyResponse(
            lobby.getId().getValue(),
            lobby.getName(),
            lobby.getPlayers().size(),
            lobby.getMaxPlayers(),
            lobby.isOpen()
        );
    }

    public record JoinLobbyCommand(String lobbyId, String playerId) {}
    
    public record LobbyResponse(
        String lobbyId,
        String name,
        int currentPlayers,
        int maxPlayers,
        boolean isOpen
    ) {}
}

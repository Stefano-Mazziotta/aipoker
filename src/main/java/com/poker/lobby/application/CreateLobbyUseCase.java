package com.poker.lobby.application;

import com.poker.lobby.domain.model.Lobby;
import com.poker.lobby.domain.repository.LobbyRepository;
import com.poker.player.domain.model.PlayerId;

/**
 * Use case for creating a new lobby.
 */
public class CreateLobbyUseCase {
    private final LobbyRepository lobbyRepository;

    public CreateLobbyUseCase(LobbyRepository lobbyRepository) {
        this.lobbyRepository = lobbyRepository;
    }

    public LobbyResponse execute(CreateLobbyCommand command) {
        // Validate max players
        if (command.maxPlayers() < 2 || command.maxPlayers() > 9) {
            throw new IllegalArgumentException("Max players must be between 2 and 9");
        }

        // Create lobby with admin
        PlayerId adminId = PlayerId.from(command.adminPlayerId());
        Lobby lobby = Lobby.create(command.name(), command.maxPlayers(), adminId);
        
        // Save to repository
        lobbyRepository.save(lobby);

        return new LobbyResponse(
            lobby.getId().getValue(),
            lobby.getName(),
            lobby.getPlayers().size(),
            lobby.getMaxPlayers(),
            lobby.isOpen(),
            adminId.getValue().toString()
        );
    }

    public record CreateLobbyCommand(String name, int maxPlayers, String adminPlayerId) {}
    
    public record LobbyResponse(
        String lobbyId,
        String name,
        int currentPlayers,
        int maxPlayers,
        boolean isOpen,
        String adminPlayerId
    ) {}
}

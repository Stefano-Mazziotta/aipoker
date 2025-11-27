package com.poker.lobby.application;

import java.util.List;
import java.util.stream.Collectors;

import com.poker.lobby.application.dto.LobbyDTO;
import com.poker.lobby.domain.model.Lobby;
import com.poker.lobby.domain.repository.LobbyRepository;
import com.poker.player.domain.model.Player;
import com.poker.player.domain.model.PlayerId;
import com.poker.player.domain.repository.PlayerRepository;

/**
 * Use case for creating a new lobby.
 */
public class CreateLobbyUseCase {
    private final LobbyRepository lobbyRepository;
    private final PlayerRepository playerRepository;

    public CreateLobbyUseCase(LobbyRepository lobbyRepository, PlayerRepository playerRepository) {
        this.lobbyRepository = lobbyRepository;
        this.playerRepository = playerRepository;
    }

    public LobbyDTO execute(CreateLobbyCommand command) {
        // Validate max players
        if (command.maxPlayers() < 2 || command.maxPlayers() > 9) {
            throw new IllegalArgumentException("Max players must be between 2 and 9");
        }

        // Load admin player
        PlayerId adminId = PlayerId.from(command.adminPlayerId());
        Player adminPlayer = playerRepository.findById(adminId)
            .orElseThrow(() -> new IllegalArgumentException("Admin player not found"));

        // Create lobby with admin
        Lobby lobby = Lobby.create(command.name(), command.maxPlayers(), adminPlayer);
        
        // Save to repository
        lobbyRepository.save(lobby);

        // Reload lobby to get fresh data (ensures consistency)
        lobby = lobbyRepository.findById(lobby.getId())
            .orElseThrow(() -> new IllegalStateException("Lobby not found after save"));

        // Build player list - now it's simple! Lobby already has players
        List<LobbyDTO.PlayerInLobbyDTO> players = lobby.getPlayers().stream()
            .map(p -> new LobbyDTO.PlayerInLobbyDTO(
                p.getId().getValue().toString(),
                p.getName()
            ))
            .collect(Collectors.toList());

        return LobbyDTO.fromDomain(
            lobby.getId().getValue(),
            lobby.getName(),
            lobby.getPlayers().size(),
            lobby.getMaxPlayers(),
            lobby.isOpen(),
            adminId.getValue().toString(),
            players
        );
    }

    public record CreateLobbyCommand(String name, int maxPlayers, String adminPlayerId) {}
}

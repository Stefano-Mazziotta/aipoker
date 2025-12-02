package com.poker.lobby.application;

import java.util.List;
import java.util.stream.Collectors;

import com.poker.lobby.application.dto.LobbyDTO;
import com.poker.lobby.application.dto.PlayerDTO;
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

        Lobby lobby = Lobby.create(command.name(), command.maxPlayers(), adminPlayer);
        
        lobbyRepository.save(lobby);

        List<PlayerDTO> players = lobby.getPlayers().stream()
            .map(p -> new PlayerDTO(
                p.getId().getValue().toString(),
                p.getName(),
                p.getChips().getAmount()
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

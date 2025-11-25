package com.poker.player.application;

import com.poker.player.application.dto.RegisterPlayerDTO;
import com.poker.player.domain.model.Player;
import com.poker.player.domain.repository.PlayerRepository;

/**
 * Use case for registering a new player.
 * Returns RegisterPlayerDTO to decouple the application layer from domain entities.
 */
public class RegisterPlayerUseCase {
    private final PlayerRepository playerRepository;

    public RegisterPlayerUseCase(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public RegisterPlayerDTO execute(RegisterPlayerCommand command) {
        // Check if player name already exists
        if (playerRepository.findByName(command.name()).isPresent()) {
            throw new IllegalArgumentException("Player name already exists: " + command.name());
        }

        // Create new player with initial chips
        Player player = Player.create(command.name(), command.initialChips());
        
        // Save to repository
        playerRepository.save(player);

        return RegisterPlayerDTO.fromDomain(
            player.getId().getValue().toString(),
            player.getName(),
            player.getChipsAmount()
        );
    }

    public record RegisterPlayerCommand(String name, int initialChips) {}
}

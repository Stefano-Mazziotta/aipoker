package com.poker.player.application;

import com.poker.player.domain.model.Player;
import com.poker.player.domain.repository.PlayerRepository;
import com.poker.shared.domain.valueobject.Chips;

/**
 * Use case for registering a new player.
 */
public class RegisterPlayerUseCase {
    private final PlayerRepository playerRepository;

    public RegisterPlayerUseCase(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public PlayerResponse execute(RegisterPlayerCommand command) {
        // Check if player name already exists
        if (playerRepository.findByName(command.name()).isPresent()) {
            throw new IllegalArgumentException("Player name already exists: " + command.name());
        }

        // Create new player with initial chips
        Player player = Player.create(command.name(), command.initialChips());
        
        // Save to repository
        playerRepository.save(player);

        return new PlayerResponse(
            player.getId().getValue().toString(),
            player.getName(),
            player.getChipsAmount()
        );
    }

    public record RegisterPlayerCommand(String name, int initialChips) {}
    
    public record PlayerResponse(String id, String name, int chips) {}
}

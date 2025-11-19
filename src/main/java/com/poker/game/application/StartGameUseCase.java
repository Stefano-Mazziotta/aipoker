package com.poker.game.application;

import com.poker.game.domain.model.*;
import com.poker.game.domain.repository.GameRepository;
import com.poker.player.domain.model.*;
import com.poker.player.domain.repository.PlayerRepository;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Use case for starting a new poker game.
 */
public class StartGameUseCase {
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;

    public StartGameUseCase(GameRepository gameRepository, PlayerRepository playerRepository) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
    }

    public GameResponse execute(StartGameCommand command) {
        // Load all players
        List<Player> players = command.playerIds().stream()
            .map(id -> playerRepository.findById(new PlayerId(id))
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + id)))
            .collect(Collectors.toList());

        // Validate minimum chips
        int minimumChips = command.blinds().getBigBlind() * 10;
        players.forEach(player -> {
            if (player.getChipsAmount() < minimumChips) {
                throw new IllegalArgumentException(
                    "Player " + player.getName() + " needs at least " + minimumChips + " chips"
                );
            }
        });

        // Create and start game
        Game game = Game.create(players, command.blinds());
        game.start();

        // Save game
        gameRepository.save(game);

        return new GameResponse(
            game.getId().getValue(),
            game.getState().name(),
            game.getPlayers().stream().map(p -> p.getName()).collect(Collectors.toList()),
            game.getCurrentPot().getAmount()
        );
    }

    public record StartGameCommand(List<String> playerIds, Blinds blinds) {}
    
    public record GameResponse(String gameId, String state, List<String> players, int pot) {}
}

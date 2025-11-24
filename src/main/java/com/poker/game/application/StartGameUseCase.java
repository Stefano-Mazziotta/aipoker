package com.poker.game.application;

import java.util.List;
import java.util.stream.Collectors;

import com.poker.game.domain.events.GameStateChangedEvent;
import com.poker.game.domain.model.Blinds;
import com.poker.game.domain.model.Game;
import com.poker.game.domain.repository.GameRepository;
import com.poker.player.domain.model.Player;
import com.poker.player.domain.model.PlayerId;
import com.poker.player.domain.repository.PlayerRepository;
import com.poker.shared.domain.events.DomainEventPublisher;

/**
 * Use case for starting a new poker game.
 */
public class StartGameUseCase {
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final DomainEventPublisher eventPublisher;

    public StartGameUseCase(GameRepository gameRepository, PlayerRepository playerRepository, 
                          DomainEventPublisher eventPublisher) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.eventPublisher = eventPublisher;
    }

    public GameResponse execute(StartGameCommand command) {
        // Load all players
        List<Player> players = command.playerIds().stream()
            .map(id -> playerRepository.findById(PlayerId.from(id))
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

        // Publish game started event
        Player currentPlayer = game.getCurrentPlayer();
        GameStateChangedEvent event = new GameStateChangedEvent(
            game.getId().getValue().toString(),
            game.getState().name(),
            currentPlayer != null ? currentPlayer.getId().getValue().toString() : null,
            currentPlayer != null ? currentPlayer.getName() : null,
            game.getCurrentPot().getAmount()
        );
        eventPublisher.publishToScope(game.getId().getValue().toString(), event);

        return new GameResponse(
            game.getId().getValue().toString(),
            game.getState().name(),
            game.getPlayers().stream().map(p -> p.getName()).collect(Collectors.toList()),
            game.getCurrentPot().getAmount()
        );
    }

    public record StartGameCommand(List<String> playerIds, Blinds blinds) {}
    
    public record GameResponse(String gameId, String state, List<String> players, int pot) {}
}

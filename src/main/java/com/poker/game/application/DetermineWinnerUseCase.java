package com.poker.game.application;

import com.poker.game.domain.events.WinnerDeterminedEvent;
import com.poker.game.domain.model.Game;
import com.poker.game.domain.model.GameId;
import com.poker.game.domain.repository.GameRepository;
import com.poker.player.domain.model.Player;
import com.poker.player.domain.repository.PlayerRepository;
import com.poker.shared.domain.events.DomainEventPublisher;

/**
 * Use case for determining the winner at showdown.
 */
public class DetermineWinnerUseCase {
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final DomainEventPublisher eventPublisher;

    public DetermineWinnerUseCase(GameRepository gameRepository, PlayerRepository playerRepository,
                                 DomainEventPublisher eventPublisher) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.eventPublisher = eventPublisher;
    }

    public WinnerResponse execute(DetermineWinnerCommand command) {
        // Load game
        Game game = gameRepository.findById(GameId.from(command.gameId()))
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        int potAmount = game.getCurrentPot().getAmount();
        
        // Determine winner (this also distributes the pot)
        Player winner = game.determineWinner();

        // Save updated game and player states
        gameRepository.save(game);
        playerRepository.save(winner);

        // Publish winner event
        WinnerDeterminedEvent event = new WinnerDeterminedEvent(
            command.gameId(),
            winner.getId().getValue().toString(),
            winner.getName(),
            "WINNER", // Hand rank would need to be exposed from Game
            potAmount
        );
        eventPublisher.publishToScope(command.gameId(), event);

        return new WinnerResponse(
            winner.getId().getValue().toString(),
            winner.getName(),
            winner.getChipsAmount(),
            potAmount
        );
    }

    public record DetermineWinnerCommand(String gameId) {}
    
    public record WinnerResponse(
        String winnerId,
        String winnerName,
        int totalChips,
        int potWon
    ) {}
}

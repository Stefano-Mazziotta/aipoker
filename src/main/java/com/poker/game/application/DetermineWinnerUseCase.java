package com.poker.game.application;

import com.poker.game.domain.model.*;
import com.poker.game.domain.repository.GameRepository;
import com.poker.player.domain.model.Player;
import com.poker.player.domain.repository.PlayerRepository;

/**
 * Use case for determining the winner at showdown.
 */
public class DetermineWinnerUseCase {
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;

    public DetermineWinnerUseCase(GameRepository gameRepository, PlayerRepository playerRepository) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
    }

    public WinnerResponse execute(DetermineWinnerCommand command) {
        // Load game
        Game game = gameRepository.findById(new GameId(command.gameId()))
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        // Determine winner
        Player winner = game.determineWinner();

        // Save updated game and player states
        gameRepository.save(game);
        playerRepository.save(winner);

        return new WinnerResponse(
            winner.getId().getValue(),
            winner.getName(),
            winner.getChipsAmount(),
            game.getCurrentPot().getAmount()
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

package com.poker.game.application;

import com.poker.game.domain.model.BettingRound;
import com.poker.game.domain.model.Game;
import com.poker.game.domain.model.GameId;
import com.poker.game.domain.model.Round;
import com.poker.game.domain.repository.GameRepository;
import com.poker.player.domain.model.Player;
import com.poker.player.domain.model.PlayerAction;

/**
 * Use case for executing player actions during a game.
 */
public class PlayerActionUseCase {
    private final GameRepository gameRepository;

    public PlayerActionUseCase(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public ActionResponse execute(PlayerActionCommand command) {
        // Load game
        Game game = gameRepository.findById(GameId.from(command.gameId()))
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        // Find player
        Player player = game.getPlayers().stream()
            .filter(p -> p.getId().getValue().toString().equals(command.playerId()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Player not in game"));

        // Validate it's player's turn
        if (!game.isPlayerTurn(player)) {
            throw new IllegalArgumentException("Not your turn! Wait for your turn to act.");
        }

        // Create betting round and execute action
        Round round = game.getCurrentRound();
        BettingRound bettingRound = new BettingRound(round, game.getState());
        
        bettingRound.executePlayerAction(
            player,
            command.action(),
            command.amount()
        );
        
        // Record action and advance turn
        game.recordPlayerAction(player);

        // Save game state
        gameRepository.save(game);

        return new ActionResponse(
            game.getState().name(),
            round.getCurrentBet(),
            round.getPot().getAmount(),
            player.isFolded()
        );
    }

    public record PlayerActionCommand(
        String gameId,
        String playerId,
        PlayerAction action,
        int amount
    ) {}
    
    public record ActionResponse(
        String gameState,
        int currentBet,
        int pot,
        boolean playerFolded
    ) {}
}

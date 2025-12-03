package com.poker.game.application;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.poker.game.application.dto.PlayerActionDTO;
import com.poker.game.domain.evaluation.HandEvaluationStrategy;
import com.poker.game.domain.evaluation.PokerHand;
import com.poker.game.domain.evaluation.TexasHoldemEvaluator;
import com.poker.game.domain.events.DealtCardsEvent;
import com.poker.game.domain.events.GameStateChangedEvent;
import com.poker.game.domain.events.PlayerActionEvent;
import com.poker.game.domain.events.RoundCompletedEvent;
import com.poker.game.domain.events.WinnerDeterminedEvent;
import com.poker.game.domain.model.BettingRound;
import com.poker.game.domain.model.Game;
import com.poker.game.domain.model.GameId;
import com.poker.game.domain.model.GameState;
import com.poker.game.domain.model.Round;
import com.poker.game.domain.repository.GameRepository;
import com.poker.player.domain.model.Player;
import com.poker.player.domain.model.PlayerAction;
import com.poker.shared.domain.events.DomainEventPublisher;
import com.poker.shared.domain.valueobject.Card;

/**
 * Use case for executing player actions during a game.
 * Returns PlayerActionDTO to decouple the application layer from domain entities.
 */
public class PlayerActionUseCase {
    private final GameRepository gameRepository;
    private final DomainEventPublisher eventPublisher;

    public PlayerActionUseCase(GameRepository gameRepository, DomainEventPublisher eventPublisher) {
        this.gameRepository = gameRepository;
        this.eventPublisher = eventPublisher;
    }

    public PlayerActionDTO execute(PlayerActionCommand command) {
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
        if (round == null) {
            throw new IllegalStateException("Game round is not initialized. Game state: " + game.getState());
        }
        
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

        // Publish event to all subscribed clients
        PlayerActionEvent event = new PlayerActionEvent(
            command.gameId(),
            command.playerId(),
            player.getName(),
            command.action().name(),
            command.amount(),
            round.getPot().getAmount(),
            round.getCurrentBet()
        );
        eventPublisher.publishToScope(command.gameId(), event);

        // Check if betting round is complete and automatically progress game
        checkAndProgressGame(game, command.gameId());

        return PlayerActionDTO.fromDomain(
            game.getState().name(),
            round.getCurrentBet(),
            round.getPot().getAmount(),
            player.isFolded()
        );
    }

    /**
     * Check if betting round is complete and automatically progress to next phase.
     * Implements the Texas Hold'em game flow:
     * PRE_FLOP complete → Deal FLOP
     * FLOP complete → Deal TURN
     * TURN complete → Deal RIVER
     * RIVER complete → SHOWDOWN (determine winner)
     */
    private void checkAndProgressGame(Game game, String gameId) {
        // Check if betting round is complete
        if (!isBettingRoundComplete(game)) {
            return; // Still waiting for more player actions
        }

        GameState currentState = game.getState();
        String nextPhase = getNextPhase(currentState);

        // Publish round completed event
        RoundCompletedEvent roundCompletedEvent = new RoundCompletedEvent(
            gameId,
            currentState.name(),
            nextPhase
        );
        eventPublisher.publishToScope(gameId, roundCompletedEvent);

        // Progress to next phase based on current state
        switch (currentState) {
            case PRE_FLOP -> dealFlopAutomatically(game, gameId);
            case FLOP -> dealTurnAutomatically(game, gameId);
            case TURN -> dealRiverAutomatically(game, gameId);
            case RIVER -> determineWinnerAutomatically(game, gameId);
            default -> {} // No automatic progression for other states
        }
    }

    private boolean isBettingRoundComplete(Game game) {
        Round round = game.getCurrentRound();
        List<Player> activePlayers = round.getActivePlayers();

        // If only one player remains (all others folded), round is complete
        if (activePlayers.size() <= 1) {
            return true;
        }

        // All active players must have acted
        for (Player player : activePlayers) {
            String playerId = player.getId().getValue().toString();
            if (!game.getPlayersActedThisRound().contains(playerId)) {
                return false;
            }
        }

        // All active players must have equal bets (or be all-in)
        int currentBet = round.getCurrentBet();
        for (Player player : activePlayers) {
            int playerBet = round.getPlayerBet(player);
            // Player must match current bet OR be all-in (0 chips)
            if (playerBet < currentBet && player.getChipsAmount() > 0) {
                return false;
            }
        }

        return true;
    }

    private String getNextPhase(GameState currentState) {
        return switch (currentState) {
            case PRE_FLOP -> "FLOP";
            case FLOP -> "TURN";
            case TURN -> "RIVER";
            case RIVER -> "SHOWDOWN";
            default -> "UNKNOWN";
        };
    }

    private void dealFlopAutomatically(Game game, String gameId) {
        int prevCount = game.getCommunityCards().size();
        game.dealFlop();
        gameRepository.save(game);
        publishCardsDealtEvent(game, "FLOP", prevCount, gameId);
        publishGameStateChanged(game, gameId);
    }

    private void dealTurnAutomatically(Game game, String gameId) {
        int prevCount = game.getCommunityCards().size();
        game.dealTurn();
        gameRepository.save(game);
        publishCardsDealtEvent(game, "TURN", prevCount, gameId);
        publishGameStateChanged(game, gameId);
    }

    private void dealRiverAutomatically(Game game, String gameId) {
        int prevCount = game.getCommunityCards().size();
        game.dealRiver();
        gameRepository.save(game);
        publishCardsDealtEvent(game, "RIVER", prevCount, gameId);
        publishGameStateChanged(game, gameId);
    }

    private void determineWinnerAutomatically(Game game, String gameId) {
        // Evaluate hands to determine winner and their hand rank
        HandEvaluationStrategy evaluator = new TexasHoldemEvaluator();
        Player winner = null;
        PokerHand bestHand = null;
        
        for (Player player : game.getPlayers()) {
            if (player.isFolded()) continue;
            
            List<Card> allCards = new ArrayList<>(player.getHand().getCards());
            allCards.addAll(game.getCommunityCards());
            
            PokerHand hand = evaluator.evaluate(allCards);
            
            if (bestHand == null || hand.compareTo(bestHand) > 0) {
                bestHand = hand;
                winner = player;
            }
        }

        // Use domain method to handle pot distribution
        game.determineWinner();
        gameRepository.save(game);

        if (winner != null && bestHand != null) {
            WinnerDeterminedEvent event = new WinnerDeterminedEvent(
                gameId,
                winner.getId().getValue().toString(),
                winner.getName(),
                bestHand.getRank().name(), // Hand rank (e.g., "FLUSH", "STRAIGHT")
                game.getCurrentPot().getAmount()
            );
            eventPublisher.publishToScope(gameId, event);
        }

        publishGameStateChanged(game, gameId);
    }

    private void publishCardsDealtEvent(Game game, String phase, int prevCount, String gameId) {
        List<Card> allCards = game.getCommunityCards();
        List<String> newCards = allCards.stream()
            .skip(prevCount)
            .map(card -> card.getRank().name() + card.getSuit().getSymbol())
            .collect(Collectors.toList());

        List<String> allCardsStr = allCards.stream()
            .map(card -> card.getRank().name() + card.getSuit().getSymbol())
            .collect(Collectors.toList());

        DealtCardsEvent event = new DealtCardsEvent(
            gameId,
            phase,
            newCards,
            allCardsStr
        );
        eventPublisher.publishToScope(gameId, event);
    }

    private void publishGameStateChanged(Game game, String gameId) {
        Player currentPlayer = game.getCurrentPlayer();
        GameStateChangedEvent event = new GameStateChangedEvent(
            gameId,
            game.getState().name(),
            currentPlayer != null ? currentPlayer.getId().getValue().toString() : null,
            currentPlayer != null ? currentPlayer.getName() : null,
            game.getCurrentPot().getAmount()
        );
        eventPublisher.publishToScope(gameId, event);
    }

    public record PlayerActionCommand(
        String gameId,
        String playerId,
        PlayerAction action,
        int amount
    ) {}
}

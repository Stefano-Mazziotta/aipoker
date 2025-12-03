package com.poker.game.application;

import java.util.List;
import java.util.stream.Collectors;

import com.poker.game.application.dto.StartGameDTO;
import com.poker.game.domain.events.GamePlayerData;
import com.poker.game.domain.events.GameStartedEvent;
import com.poker.game.domain.events.GameStateChangedEvent;
import com.poker.game.domain.events.PlayerCardsDealtEvent;
import com.poker.game.domain.model.Game;
import com.poker.game.domain.model.GameId;
import com.poker.game.domain.repository.GameRepository;
import com.poker.player.domain.model.Player;
import com.poker.shared.domain.events.DomainEventPublisher;

/**
 * Use case for starting a new hand in an ongoing game.
 * Handles dealer advancement, player elimination, and game termination.
 */
public class StartNewHandUseCase {
    private final GameRepository gameRepository;
    private final DomainEventPublisher eventPublisher;

    public StartNewHandUseCase(GameRepository gameRepository, DomainEventPublisher eventPublisher) {
        this.gameRepository = gameRepository;
        this.eventPublisher = eventPublisher;
    }

    public StartGameDTO execute(StartNewHandCommand command) {
        // Load game
        Game game = gameRepository.findById(GameId.from(command.gameId()))
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        // Advance dealer button
        game.advanceDealer();

        // Eliminate players with 0 chips
        List<Player> remainingPlayers = game.getPlayers().stream()
            .filter(p -> p.getChipsAmount() > 0)
            .collect(Collectors.toList());

        // Check if game should end (only 1 player remaining)
        if (remainingPlayers.size() <= 1) {
            // Game over - publish final winner
            if (!remainingPlayers.isEmpty()) {
                Player finalWinner = remainingPlayers.get(0);
                GameStateChangedEvent event = new GameStateChangedEvent(
                    command.gameId(),
                    "FINISHED",
                    finalWinner.getId().getValue().toString(),
                    finalWinner.getName(),
                    0,
                    0,
                    List.of()
                );
                eventPublisher.publishToScope(command.gameId(), event);
                
                // Build response DTO for finished game
                List<StartGameDTO.PlayerGameStateDTO> finishedPlayerDTOs = remainingPlayers.stream()
                    .map(p -> new StartGameDTO.PlayerGameStateDTO(
                        p.getId().getValue().toString(),
                        p.getName(),
                        p.getChipsAmount(),
                        0,
                        p.isFolded(),
                        p.isAllIn()
                    ))
                    .collect(Collectors.toList());

                return StartGameDTO.fromDomain(
                    command.gameId(),
                    command.lobbyId(),
                    finishedPlayerDTOs,
                    game.getBlinds().getSmallBlind(),
                    game.getBlinds().getBigBlind(),
                    0,
                    0,
                    finalWinner.getId().getValue().toString(),
                    finalWinner.getName(),
                    "FINISHED"
                );
            }
            
            // No players remaining - return empty game
            return StartGameDTO.fromDomain(
                command.gameId(),
                command.lobbyId(),
                List.of(),
                game.getBlinds().getSmallBlind(),
                game.getBlinds().getBigBlind(),
                0,
                0,
                "",
                "",
                "FINISHED"
            );
        }

        // Start new hand (resets deck, community cards, player hands)
        // This will internally call resetForNewHand(), postBlinds(), dealHoleCards()
        // But we need to expose a method in Game for this
        // For now, we'll just reset the state and publish events
        
        // Save game
        gameRepository.save(game);

        String gameId = game.getId().getValue().toString();

        // Prepare player data for event
        List<GamePlayerData> playerDataList = remainingPlayers.stream()
            .map(player -> new GamePlayerData(
                player.getId().getValue().toString(),
                player.getName(),
                player.getChipsAmount(),
                0, // currentBet - will be updated after first action
                player.isFolded(),
                player.isAllIn()
            ))
            .collect(Collectors.toList());

        // Get current player information
        Player currentPlayer = game.getCurrentPlayer();
        String currentPlayerId = currentPlayer != null ? currentPlayer.getId().getValue().toString() : "";
        String currentPlayerName = currentPlayer != null ? currentPlayer.getName() : "";
        int currentBet = game.getCurrentRound() != null ? game.getCurrentRound().getCurrentBet() : 0;

        // Publish GAME_STARTED event for new hand
        GameStartedEvent gameStartedEvent = new GameStartedEvent(
            gameId,
            command.lobbyId(),
            playerDataList,
            game.getBlinds().getSmallBlind(),
            game.getBlinds().getBigBlind(),
            game.getCurrentPot().getAmount(),
            currentBet,
            currentPlayerId,
            currentPlayerName,
            game.getState().name()
        );
        eventPublisher.publishToScope(command.lobbyId(), gameStartedEvent);

        // Publish initial state
        List<String> communityCardsStr = game.getCommunityCards().stream()
            .map(card -> card.getRank().name() + card.getSuit().getSymbol())
            .collect(Collectors.toList());
        
        GameStateChangedEvent stateChangedEvent = new GameStateChangedEvent(
            gameId,
            game.getState().name(),
            currentPlayerId,
            currentPlayerName,
            game.getCurrentPot().getAmount(),
            game.getCurrentRound().getCurrentBet(),
            communityCardsStr
        );
        eventPublisher.publishToScope(gameId, stateChangedEvent);

        // Publish individual player cards to each player (private events)
        remainingPlayers.forEach(player -> {
            List<String> playerCards = player.getHand().getCards().stream()
                .map(card -> card.getRank().name() + card.getSuit().getSymbol())
                .collect(Collectors.toList());
            
            PlayerCardsDealtEvent cardsEvent = new PlayerCardsDealtEvent(
                gameId,
                player.getId().getValue().toString(),
                playerCards
            );
            eventPublisher.publishToPlayer(player.getId().getValue().toString(), cardsEvent);
        });

        // Build response DTO with same structure as event data
        List<StartGameDTO.PlayerGameStateDTO> playerDTOs = playerDataList.stream()
            .map(p -> new StartGameDTO.PlayerGameStateDTO(
                p.getPlayerId(),
                p.getPlayerName(),
                p.getChips(),
                p.getCurrentBet(),
                p.isFolded(),
                p.isAllIn()
            ))
            .collect(Collectors.toList());

        return StartGameDTO.fromDomain(
            gameId,
            command.lobbyId(),
            playerDTOs,
            game.getBlinds().getSmallBlind(),
            game.getBlinds().getBigBlind(),
            game.getCurrentPot().getAmount(),
            currentBet,
            currentPlayerId,
            currentPlayerName,
            game.getState().name()
        );
    }

    public record StartNewHandCommand(String gameId, String lobbyId) {}
}

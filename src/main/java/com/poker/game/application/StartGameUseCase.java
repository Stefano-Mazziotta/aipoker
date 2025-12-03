package com.poker.game.application;

import java.util.List;
import java.util.stream.Collectors;

import com.poker.game.application.dto.StartGameDTO;
import com.poker.game.domain.events.GamePlayerData;
import com.poker.game.domain.events.GameStartedEvent;
import com.poker.game.domain.events.GameStateChangedEvent;
import com.poker.game.domain.events.PlayerCardsDealtEvent;
import com.poker.game.domain.model.Blinds;
import com.poker.game.domain.model.Game;
import com.poker.game.domain.repository.GameRepository;
import com.poker.lobby.domain.model.LobbyId;
import com.poker.player.domain.model.Player;
import com.poker.player.domain.model.PlayerId;
import com.poker.player.domain.repository.PlayerRepository;
import com.poker.shared.domain.events.DomainEventPublisher;

/**
 * Use case for starting a new poker game.
 * Returns StartGameDTO to decouple the application layer from domain entities.
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

    public StartGameDTO execute(StartGameCommand command) {
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

        String gameId = game.getId().getValue().toString();
        String lobbyIdStr = command.lobbyId().getValue();

        // Prepare player data for event
        List<GamePlayerData> playerDataList = game.getPlayers().stream()
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

        // Publish GAME_STARTED event to lobby scope (all players in lobby receive it)
        // This event contains complete game state so clients can render the game immediately
        GameStartedEvent gameStartedEvent = new GameStartedEvent(
            gameId,
            lobbyIdStr,
            playerDataList,
            command.blinds().getSmallBlind(),
            command.blinds().getBigBlind(),
            game.getCurrentPot().getAmount(),
            currentBet,
            currentPlayerId,
            currentPlayerName,
            game.getState().name()
        );
        eventPublisher.publishToScope(lobbyIdStr, gameStartedEvent);

        // Publish GAME_STATE_CHANGED event to game scope (initial state)
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
        game.getPlayers().forEach(player -> {
            List<String> playerCards = player.getHand().getCards().stream()
                .map(card -> card.getRank().name() + card.getSuit().getSymbol())
                .collect(Collectors.toList());
            
            PlayerCardsDealtEvent cardsEvent = new PlayerCardsDealtEvent(
                gameId,
                player.getId().getValue().toString(),
                playerCards
            );
            // Publish to player's personal scope
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
            lobbyIdStr,
            playerDTOs,
            command.blinds().getSmallBlind(),
            command.blinds().getBigBlind(),
            game.getCurrentPot().getAmount(),
            currentBet,
            currentPlayerId,
            currentPlayerName,
            game.getState().name()
        );
    }

    public record StartGameCommand(List<String> playerIds, Blinds blinds, LobbyId lobbyId) {}
}

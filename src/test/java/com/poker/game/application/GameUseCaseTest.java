package com.poker.game.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.poker.game.application.dto.PlayerActionDTO;
import com.poker.game.application.dto.StartGameDTO;
import com.poker.game.domain.model.Blinds;
import com.poker.game.domain.model.Game;
import com.poker.game.domain.model.GameId;
import com.poker.game.domain.model.GameState;
import com.poker.game.domain.repository.GameRepository;
import com.poker.player.domain.model.Player;
import com.poker.player.domain.model.PlayerAction;
import com.poker.player.domain.model.PlayerId;
import com.poker.player.domain.repository.PlayerRepository;
import com.poker.shared.domain.events.DomainEventPublisher;
import com.poker.shared.domain.events.NoOpEventPublisher;

/**
 * Integration tests for game use cases.
 * 
 * These tests validate the behavior of the application layer use cases,
 * working exclusively with DTOs to decouple tests from domain implementation details.
 * This approach ensures tests remain stable even when domain models evolve.
 */
class GameUseCaseTest {

    private StartGameUseCase startGameUseCase;
    private PlayerActionUseCase playerActionUseCase;

    private InMemoryGameRepository gameRepository;
    private InMemoryPlayerRepository playerRepository;
    private DomainEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        gameRepository = new InMemoryGameRepository();
        playerRepository = new InMemoryPlayerRepository();
        eventPublisher = new NoOpEventPublisher();

        startGameUseCase = new StartGameUseCase(gameRepository, playerRepository, eventPublisher);
        playerActionUseCase = new PlayerActionUseCase(gameRepository, eventPublisher);
    }

    @Test
    void testCompleteGameFlow() {
        // Create and save players
        Player alice = Player.create("Alice", 1000);
        Player bob = Player.create("Bob", 1000);
        playerRepository.save(alice);
        playerRepository.save(bob);

        List<String> playerIds = List.of(
                alice.getId().getValue().toString(),
                bob.getId().getValue().toString()
        );

        // Start game - receives StartGameDTO
        var startCommand = new StartGameUseCase.StartGameCommand(
                playerIds,
                new Blinds(10, 20)
        );
        StartGameDTO startGameDTO = startGameUseCase.execute(startCommand);

        // Assert on DTO fields - test works with DTOs, not domain entities
        assertNotNull(startGameDTO.gameId());
        assertEquals(2, startGameDTO.players().size());

        // Get the game to check current player (domain layer check for test setup)
        Game game = gameRepository.findById(GameId.from(startGameDTO.gameId())).orElseThrow();
        Player currentPlayer = game.getCurrentPlayer();
        assertNotNull(currentPlayer);

        // Current player folds - receives PlayerActionDTO
        var foldCommand = new PlayerActionUseCase.PlayerActionCommand(
                startGameDTO.gameId(),
                currentPlayer.getId().getValue().toString(),
                PlayerAction.FOLD,
                0
        );
        PlayerActionDTO playerActionDTO = playerActionUseCase.execute(foldCommand);

        // Assert on DTO fields - verify fold was successful
        assertNotNull(playerActionDTO);
        assertTrue(playerActionDTO.playerFolded());
    }

    @Test
    void testPlayerFold() {
        Player alice = Player.create("Alice", 1000);
        Player bob = Player.create("Bob", 1000);
        playerRepository.save(alice);
        playerRepository.save(bob);

        var startCommand = new StartGameUseCase.StartGameCommand(
                List.of(
                        alice.getId().getValue().toString(),
                        bob.getId().getValue().toString()
                ),
                new Blinds(10, 20)
        );
        // Start game - receives StartGameDTO
        StartGameDTO startGameDTO = startGameUseCase.execute(startCommand);

        // Get the game to check current player (domain layer check for test setup)
        Game game = gameRepository.findById(GameId.from(startGameDTO.gameId())).orElseThrow();
        Player currentPlayer = game.getCurrentPlayer();
        assertNotNull(currentPlayer);

        // Current player folds - receives PlayerActionDTO
        var foldCommand = new PlayerActionUseCase.PlayerActionCommand(
                startGameDTO.gameId(),
                currentPlayer.getId().getValue().toString(),
                PlayerAction.FOLD,
                0
        );
        PlayerActionDTO playerActionDTO = playerActionUseCase.execute(foldCommand);

        // Assert on DTO fields
        assertTrue(playerActionDTO.playerFolded());

        // Reload game and verify player is folded (domain layer verification)
        game = gameRepository.findById(GameId.from(startGameDTO.gameId())).orElseThrow();
        Player foldedPlayer = game.getPlayers().stream()
                .filter(p -> p.getId().equals(currentPlayer.getId()))
                .findFirst()
                .orElseThrow();
        assertTrue(foldedPlayer.isFolded());
    }

    // Simple in-memory repositories for testing
    static class InMemoryGameRepository implements GameRepository {

        private final Map<GameId, Game> games = new HashMap<>();

        @Override
        public void save(Game game) {
            games.put(game.getId(), game);
        }

        @Override
        public Optional<Game> findById(GameId id) {
            return Optional.ofNullable(games.get(id));
        }

        @Override
        public List<Game> findByState(GameState state) {
            return games.values().stream()
                    .filter(g -> g.getState() == state)
                    .toList();
        }

        @Override
        public List<Game> findActiveGames() {
            return games.values().stream()
                    .filter(g -> g.getState() != GameState.FINISHED)
                    .toList();
        }

        @Override
        public List<Game> findByPlayer(PlayerId playerId) {
            return games.values().stream()
                    .filter(g -> g.getPlayers().stream()
                    .anyMatch(p -> p.getId().equals(playerId)))
                    .toList();
        }

        @Override
        public boolean exists(GameId id) {
            return games.containsKey(id);
        }

        @Override
        public void delete(GameId id) {
            games.remove(id);
        }

        @Override
        public List<Game> findAll() {
            return new ArrayList<>(games.values());
        }
    }

    static class InMemoryPlayerRepository implements PlayerRepository {

        private final Map<PlayerId, Player> players = new HashMap<>();

        @Override
        public void save(Player player) {
            players.put(player.getId(), player);
        }

        @Override
        public Optional<Player> findById(PlayerId id) {
            return Optional.ofNullable(players.get(id));
        }

        @Override
        public Optional<Player> findByName(String name) {
            return players.values().stream()
                    .filter(p -> p.getName().equals(name))
                    .findFirst();
        }

        @Override
        public List<Player> findAll() {
            return new ArrayList<>(players.values());
        }

        @Override
        public boolean exists(PlayerId id) {
            return players.containsKey(id);
        }

        @Override
        public void delete(PlayerId id) {
            players.remove(id);
        }

        @Override
        public List<Player> findTopByChips(int limit) {
            return players.values().stream()
                    .sorted((p1, p2) -> Integer.compare(p2.getChipsAmount(), p1.getChipsAmount()))
                    .limit(limit)
                    .toList();
        }
    }
}

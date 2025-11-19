package com.poker.game.application;

import com.poker.game.domain.model.*;
import com.poker.game.domain.repository.GameRepository;
import com.poker.player.domain.model.Player;
import com.poker.player.domain.model.PlayerId;
import com.poker.player.domain.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for game use cases.
 */
class GameUseCaseTest {

    private StartGameUseCase startGameUseCase;
    private DealCardsUseCase dealCardsUseCase;
    private PlayerActionUseCase playerActionUseCase;
    private DetermineWinnerUseCase determineWinnerUseCase;
    
    private InMemoryGameRepository gameRepository;
    private InMemoryPlayerRepository playerRepository;

    @BeforeEach
    void setUp() {
        gameRepository = new InMemoryGameRepository();
        playerRepository = new InMemoryPlayerRepository();
        
        startGameUseCase = new StartGameUseCase(gameRepository, playerRepository);
        dealCardsUseCase = new DealCardsUseCase(gameRepository);
        playerActionUseCase = new PlayerActionUseCase(gameRepository, playerRepository);
        determineWinnerUseCase = new DetermineWinnerUseCase(gameRepository, playerRepository);
    }

    @Test
    void testCompleteGameFlow() {
        // Create and save players
        Player alice = Player.create("Alice", 1000);
        Player bob = Player.create("Bob", 1000);
        playerRepository.save(alice);
        playerRepository.save(bob);
        
        List<String> playerIds = List.of(
            alice.getId().getValue(), 
            bob.getId().getValue()
        );
        
        // Start game
        var startCommand = new StartGameUseCase.StartGameCommand(playerIds, 10, 20);
        var startResponse = startGameUseCase.execute(startCommand);
        
        assertNotNull(startResponse.gameId());
        assertEquals(2, startResponse.players().size());
        
        // Deal flop
        var flopCommand = new DealCardsUseCase.DealCardsCommand(
            startResponse.gameId(), 
            DealCardsUseCase.DealType.FLOP
        );
        var flopResponse = dealCardsUseCase.execute(flopCommand);
        
        assertEquals(3, flopResponse.dealtCards().size());
        
        // Player action - check
        var checkCommand = new PlayerActionUseCase.PlayerActionCommand(
            startResponse.gameId(),
            alice.getId().getValue(),
            PlayerActionUseCase.ActionType.CHECK,
            null
        );
        var checkResponse = playerActionUseCase.execute(checkCommand);
        
        assertTrue(checkResponse.success());
        
        // Deal turn
        var turnCommand = new DealCardsUseCase.DealCardsCommand(
            startResponse.gameId(), 
            DealCardsUseCase.DealType.TURN
        );
        var turnResponse = dealCardsUseCase.execute(turnCommand);
        
        assertEquals(1, turnResponse.dealtCards().size());
        
        // Deal river
        var riverCommand = new DealCardsUseCase.DealCardsCommand(
            startResponse.gameId(), 
            DealCardsUseCase.DealType.RIVER
        );
        var riverResponse = dealCardsUseCase.execute(riverCommand);
        
        assertEquals(1, riverResponse.dealtCards().size());
        
        // Determine winner
        var winnerCommand = new DetermineWinnerUseCase.DetermineWinnerCommand(startResponse.gameId());
        var winnerResponse = determineWinnerUseCase.execute(winnerCommand);
        
        assertNotNull(winnerResponse.winnerId());
        assertTrue(winnerResponse.winAmount() > 0);
    }

    @Test
    void testPlayerFold() {
        Player alice = Player.create("Alice", 1000);
        Player bob = Player.create("Bob", 1000);
        playerRepository.save(alice);
        playerRepository.save(bob);
        
        var startCommand = new StartGameUseCase.StartGameCommand(
            List.of(alice.getId().getValue(), bob.getId().getValue()),
            10, 20
        );
        var startResponse = startGameUseCase.execute(startCommand);
        
        // Alice folds
        var foldCommand = new PlayerActionUseCase.PlayerActionCommand(
            startResponse.gameId(),
            alice.getId().getValue(),
            PlayerActionUseCase.ActionType.FOLD,
            null
        );
        var foldResponse = playerActionUseCase.execute(foldCommand);
        
        assertTrue(foldResponse.success());
        
        // Verify Alice is folded
        Game game = gameRepository.findById(new GameId(startResponse.gameId())).orElseThrow();
        Player aliceInGame = playerRepository.findById(alice.getId()).orElseThrow();
        assertTrue(aliceInGame.isFolded());
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
        public List<Game> findAll() {
            return new ArrayList<>(games.values());
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
        public List<Game> findByStatus(GameStatus status) {
            return games.values().stream()
                .filter(g -> g.getStatus() == status)
                .toList();
        }

        @Override
        public Optional<Game> findActiveGameByPlayerId(PlayerId playerId) {
            return games.values().stream()
                .filter(g -> g.getStatus() == GameStatus.IN_PROGRESS)
                .filter(g -> g.getPlayerIds().contains(playerId))
                .findFirst();
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

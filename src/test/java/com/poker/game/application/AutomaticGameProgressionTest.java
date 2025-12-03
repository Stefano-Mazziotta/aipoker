package com.poker.game.application;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.poker.game.application.dto.StartGameDTO;
import com.poker.game.domain.model.Blinds;
import com.poker.game.domain.model.Game;
import com.poker.game.domain.model.GameId;
import com.poker.game.domain.model.GameState;
import com.poker.lobby.domain.model.LobbyId;
import com.poker.player.domain.model.Player;
import com.poker.player.domain.model.PlayerAction;
import com.poker.shared.domain.enums.EventTypeEnum;
import com.poker.shared.domain.events.DomainEvent;
import com.poker.shared.domain.events.DomainEventPublisher;

/**
 * Unit tests for automatic game progression in PlayerActionUseCase.
 * Tests the core logic of betting round completion detection and automatic phase transitions.
 */
class AutomaticGameProgressionTest {

    private StartGameUseCase startGameUseCase;
    private PlayerActionUseCase playerActionUseCase;
    private InMemoryGameRepository gameRepository;
    private InMemoryPlayerRepository playerRepository;
    private TestEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        gameRepository = new InMemoryGameRepository();
        playerRepository = new InMemoryPlayerRepository();
        eventPublisher = new TestEventPublisher();

        startGameUseCase = new StartGameUseCase(gameRepository, playerRepository, eventPublisher);
        playerActionUseCase = new PlayerActionUseCase(gameRepository, eventPublisher);
    }

    @Test
    void testAutomaticProgressionFromPreFlopToFlop() {
        // Setup: Create game with 3 players in PRE_FLOP
        StartGameDTO gameDTO = setupGameWithPlayers(3);
        Game game = gameRepository.findById(GameId.from(gameDTO.gameId())).orElseThrow();
        
        assertEquals(GameState.PRE_FLOP, game.getState());
        assertEquals(0, game.getCommunityCards().size());
        
        // Execute: All players call to complete PRE_FLOP round
        completePreFlopBetting(game, gameDTO.gameId());
        
        // Verify: Game automatically progressed to FLOP
        game = gameRepository.findById(GameId.from(gameDTO.gameId())).orElseThrow();
        assertEquals(GameState.FLOP, game.getState());
        assertEquals(3, game.getCommunityCards().size(), "FLOP should have 3 community cards");
        
        // Verify: Events were published
        assertTrue(eventPublisher.hasEventOfType(EventTypeEnum.ROUND_COMPLETED));
        assertTrue(eventPublisher.hasEventOfType(EventTypeEnum.DEALT_CARDS));
        assertTrue(eventPublisher.hasEventOfType(EventTypeEnum.GAME_STATE_CHANGED));
    }

    @Test
    void testAutomaticProgressionFromFlopToTurn() {
        // Setup: Start game and progress to FLOP
        StartGameDTO gameDTO = setupGameWithPlayers(3);
        Game game = gameRepository.findById(GameId.from(gameDTO.gameId())).orElseThrow();
        completePreFlopBetting(game, gameDTO.gameId());
        
        game = gameRepository.findById(GameId.from(gameDTO.gameId())).orElseThrow();
        assertEquals(GameState.FLOP, game.getState());
        assertEquals(3, game.getCommunityCards().size());
        
        eventPublisher.clear();
        
        // Execute: All players check to complete FLOP round
        completeBettingRoundWithChecks(game, gameDTO.gameId());
        
        // Verify: Game automatically progressed to TURN
        game = gameRepository.findById(GameId.from(gameDTO.gameId())).orElseThrow();
        assertEquals(GameState.TURN, game.getState());
        assertEquals(4, game.getCommunityCards().size(), "TURN should have 4 community cards");
        
        assertTrue(eventPublisher.hasEventOfType(EventTypeEnum.ROUND_COMPLETED));
        assertTrue(eventPublisher.hasEventOfType(EventTypeEnum.DEALT_CARDS));
    }

    @Test
    void testAutomaticProgressionFromTurnToRiver() {
        // Setup: Progress to TURN
        StartGameDTO gameDTO = setupGameWithPlayers(3);
        Game game = gameRepository.findById(GameId.from(gameDTO.gameId())).orElseThrow();
        completePreFlopBetting(game, gameDTO.gameId());
        game = gameRepository.findById(GameId.from(gameDTO.gameId())).orElseThrow();
        completeBettingRoundWithChecks(game, gameDTO.gameId());
        
        game = gameRepository.findById(GameId.from(gameDTO.gameId())).orElseThrow();
        assertEquals(GameState.TURN, game.getState());
        
        eventPublisher.clear();
        
        // Execute: All players check to complete TURN round
        completeBettingRoundWithChecks(game, gameDTO.gameId());
        
        // Verify: Game automatically progressed to RIVER
        game = gameRepository.findById(GameId.from(gameDTO.gameId())).orElseThrow();
        assertEquals(GameState.RIVER, game.getState());
        assertEquals(5, game.getCommunityCards().size(), "RIVER should have 5 community cards");
        
        assertTrue(eventPublisher.hasEventOfType(EventTypeEnum.ROUND_COMPLETED));
        assertTrue(eventPublisher.hasEventOfType(EventTypeEnum.DEALT_CARDS));
    }

    @Test
    void testAutomaticProgressionFromRiverToShowdown() {
        // Setup: Progress to RIVER
        StartGameDTO gameDTO = setupGameWithPlayers(3);
        Game game = gameRepository.findById(GameId.from(gameDTO.gameId())).orElseThrow();
        completePreFlopBetting(game, gameDTO.gameId());
        game = gameRepository.findById(GameId.from(gameDTO.gameId())).orElseThrow();
        completeBettingRoundWithChecks(game, gameDTO.gameId()); // FLOP
        game = gameRepository.findById(GameId.from(gameDTO.gameId())).orElseThrow();
        completeBettingRoundWithChecks(game, gameDTO.gameId()); // TURN
        
        game = gameRepository.findById(GameId.from(gameDTO.gameId())).orElseThrow();
        assertEquals(GameState.RIVER, game.getState());
        
        eventPublisher.clear();
        
        // Execute: All players check to complete RIVER round
        completeBettingRoundWithChecks(game, gameDTO.gameId());
        
        // Verify: Game automatically progressed to SHOWDOWN and winner determined
        game = gameRepository.findById(GameId.from(gameDTO.gameId())).orElseThrow();
        assertEquals(GameState.FINISHED, game.getState());
        
        assertTrue(eventPublisher.hasEventOfType(EventTypeEnum.ROUND_COMPLETED));
        assertTrue(eventPublisher.hasEventOfType(EventTypeEnum.WINNER_DETERMINED));
    }

    @Test
    void testEarlyRoundCompletionWhenOnlyOnePlayerRemains() {
        // Setup: 3-player game
        StartGameDTO gameDTO = setupGameWithPlayers(3);
        Game game = gameRepository.findById(GameId.from(gameDTO.gameId())).orElseThrow();
        
        // Execute: Two players fold, leaving one player
        Player player1 = game.getCurrentPlayer();
        playerActionUseCase.execute(new PlayerActionUseCase.PlayerActionCommand(
            gameDTO.gameId(), player1.getId().getValue().toString(), PlayerAction.FOLD, 0
        ));
        
        game = gameRepository.findById(GameId.from(gameDTO.gameId())).orElseThrow();
        Player player2 = game.getCurrentPlayer();
        playerActionUseCase.execute(new PlayerActionUseCase.PlayerActionCommand(
            gameDTO.gameId(), player2.getId().getValue().toString(), PlayerAction.FOLD, 0
        ));
        
        // Verify: Round completes immediately and progresses to FLOP
        game = gameRepository.findById(GameId.from(gameDTO.gameId())).orElseThrow();
        assertEquals(GameState.FLOP, game.getState());
        assertTrue(eventPublisher.hasEventOfType(EventTypeEnum.ROUND_COMPLETED));
    }

    @Test
    void testNoProgressionWhenBettingRoundIncomplete() {
        // Setup: 3-player game
        StartGameDTO gameDTO = setupGameWithPlayers(3);
        Game game = gameRepository.findById(GameId.from(gameDTO.gameId())).orElseThrow();
        
        // Execute: Only one player acts
        Player currentPlayer = game.getCurrentPlayer();
        playerActionUseCase.execute(new PlayerActionUseCase.PlayerActionCommand(
            gameDTO.gameId(), currentPlayer.getId().getValue().toString(), PlayerAction.CALL, 20
        ));
        
        // Verify: Game still in PRE_FLOP (not all players acted)
        game = gameRepository.findById(GameId.from(gameDTO.gameId())).orElseThrow();
        assertEquals(GameState.PRE_FLOP, game.getState());
        assertEquals(0, game.getCommunityCards().size());
    }

    @Test
    void testNoProgressionWhenBetsNotMatched() {
        // Setup: 3-player game
        StartGameDTO gameDTO = setupGameWithPlayers(3);
        Game game = gameRepository.findById(GameId.from(gameDTO.gameId())).orElseThrow();
        
        // Execute: Player raises, creating unmatched bet
        Player currentPlayer = game.getCurrentPlayer();
        playerActionUseCase.execute(new PlayerActionUseCase.PlayerActionCommand(
            gameDTO.gameId(), currentPlayer.getId().getValue().toString(), PlayerAction.RAISE, 50
        ));
        
        game = gameRepository.findById(GameId.from(gameDTO.gameId())).orElseThrow();
        currentPlayer = game.getCurrentPlayer();
        playerActionUseCase.execute(new PlayerActionUseCase.PlayerActionCommand(
            gameDTO.gameId(), currentPlayer.getId().getValue().toString(), PlayerAction.CALL, 50
        ));
        
        // Verify: Game still in PRE_FLOP (not all bets matched)
        game = gameRepository.findById(GameId.from(gameDTO.gameId())).orElseThrow();
        assertEquals(GameState.PRE_FLOP, game.getState());
    }

    @Test
    void testCompleteGameFlowWithAutomaticProgression() {
        // Setup: 3-player game
        StartGameDTO gameDTO = setupGameWithPlayers(3);
        Game game = gameRepository.findById(GameId.from(gameDTO.gameId())).orElseThrow();
        
        // Execute: Play through entire game with automatic progression
        // PRE_FLOP → FLOP
        completePreFlopBetting(game, gameDTO.gameId());
        game = gameRepository.findById(GameId.from(gameDTO.gameId())).orElseThrow();
        assertEquals(GameState.FLOP, game.getState());
        
        // FLOP → TURN
        completeBettingRoundWithChecks(game, gameDTO.gameId());
        game = gameRepository.findById(GameId.from(gameDTO.gameId())).orElseThrow();
        assertEquals(GameState.TURN, game.getState());
        
        // TURN → RIVER
        completeBettingRoundWithChecks(game, gameDTO.gameId());
        game = gameRepository.findById(GameId.from(gameDTO.gameId())).orElseThrow();
        assertEquals(GameState.RIVER, game.getState());
        
        // RIVER → SHOWDOWN
        completeBettingRoundWithChecks(game, gameDTO.gameId());
        game = gameRepository.findById(GameId.from(gameDTO.gameId())).orElseThrow();
        assertEquals(GameState.FINISHED, game.getState());
        
        // Verify: All expected events were published
        assertTrue(eventPublisher.hasEventOfType(EventTypeEnum.ROUND_COMPLETED));
        assertTrue(eventPublisher.hasEventOfType(EventTypeEnum.DEALT_CARDS));
        assertTrue(eventPublisher.hasEventOfType(EventTypeEnum.WINNER_DETERMINED));
    }

    // Helper methods

    private StartGameDTO setupGameWithPlayers(int playerCount) {
        LobbyId lobbyId = LobbyId.generate();
        List<Player> players = createPlayers(playerCount);
        players.forEach(playerRepository::save);
        
        List<String> playerIds = players.stream()
            .map(p -> p.getId().getValue().toString())
            .toList();
        
        return startGameUseCase.execute(new StartGameUseCase.StartGameCommand(
            playerIds, new Blinds(10, 20), lobbyId
        ));
    }

    private List<Player> createPlayers(int count) {
        List<Player> players = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            players.add(Player.create("Player" + (i + 1), 1000));
        }
        return players;
    }

    private void completePreFlopBetting(Game game, String gameId) {
        // All players call the big blind (20)
        int playerCount = game.getPlayers().size();
        for (int i = 0; i < playerCount; i++) {
            game = gameRepository.findById(GameId.from(gameId)).orElseThrow();
            Player currentPlayer = game.getCurrentPlayer();
            playerActionUseCase.execute(new PlayerActionUseCase.PlayerActionCommand(
                gameId, currentPlayer.getId().getValue().toString(), PlayerAction.CALL, 20
            ));
        }
    }

    private void completeBettingRoundWithChecks(Game game, String gameId) {
        // All players check
        int playerCount = game.getPlayers().size();
        for (int i = 0; i < playerCount; i++) {
            game = gameRepository.findById(GameId.from(gameId)).orElseThrow();
            Player currentPlayer = game.getCurrentPlayer();
            playerActionUseCase.execute(new PlayerActionUseCase.PlayerActionCommand(
                gameId, currentPlayer.getId().getValue().toString(), PlayerAction.CHECK, 0
            ));
        }
    }

    // Test doubles

    static class InMemoryGameRepository implements com.poker.game.domain.repository.GameRepository {
        private final java.util.Map<GameId, Game> games = new java.util.HashMap<>();

        @Override
        public void save(Game game) {
            games.put(game.getId(), game);
        }

        @Override
        public java.util.Optional<Game> findById(GameId id) {
            return java.util.Optional.ofNullable(games.get(id));
        }

        @Override
        public List<Game> findAll() {
            return List.copyOf(games.values());
        }

        @Override
        public List<Game> findByState(com.poker.game.domain.model.GameState state) {
            return games.values().stream()
                .filter(g -> g.getState() == state)
                .toList();
        }

        @Override
        public List<Game> findActiveGames() {
            return games.values().stream()
                .filter(g -> g.getState() != com.poker.game.domain.model.GameState.FINISHED)
                .toList();
        }

        @Override
        public List<Game> findByPlayer(com.poker.player.domain.model.PlayerId playerId) {
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
    }

    static class InMemoryPlayerRepository implements com.poker.player.domain.repository.PlayerRepository {
        private final java.util.Map<com.poker.player.domain.model.PlayerId, Player> players = new java.util.HashMap<>();

        @Override
        public void save(Player player) {
            players.put(player.getId(), player);
        }

        @Override
        public java.util.Optional<Player> findById(com.poker.player.domain.model.PlayerId id) {
            return java.util.Optional.ofNullable(players.get(id));
        }

        @Override
        public List<Player> findAll() {
            return List.copyOf(players.values());
        }

        @Override
        public java.util.Optional<Player> findByName(String name) {
            return players.values().stream()
                .filter(p -> p.getName().equals(name))
                .findFirst();
        }

        @Override
        public boolean exists(com.poker.player.domain.model.PlayerId id) {
            return players.containsKey(id);
        }

        @Override
        public void delete(com.poker.player.domain.model.PlayerId id) {
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

    static class TestEventPublisher implements DomainEventPublisher {
        private final List<DomainEvent> publishedEvents = new java.util.ArrayList<>();

        @Override
        public void publishToScope(String scopeId, DomainEvent event) {
            publishedEvents.add(event);
        }

        @Override
        public void publishToPlayer(String playerId, DomainEvent event) {
            publishedEvents.add(event);
        }

        @Override
        public void unsubscribeFromScope(String scopeId, String playerId) {
            // No-op for testing
        }

        public boolean hasEventOfType(EventTypeEnum eventType) {
            return publishedEvents.stream()
                .anyMatch(e -> e.eventType() == eventType);
        }

        public void clear() {
            publishedEvents.clear();
        }

        public List<DomainEvent> getPublishedEvents() {
            return List.copyOf(publishedEvents);
        }
    }
}

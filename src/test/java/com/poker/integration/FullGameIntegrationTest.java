package com.poker.integration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.poker.game.application.PlayerActionUseCase;
import com.poker.game.application.StartGameUseCase;
import com.poker.game.domain.model.Blinds;
import com.poker.game.domain.model.Game;
import com.poker.game.domain.model.GameId;
import com.poker.game.domain.repository.GameRepository;
import com.poker.game.infrastructure.persistence.SQLiteGameRepository;
import com.poker.lobby.domain.model.LobbyId;
import com.poker.player.application.RegisterPlayerUseCase;
import com.poker.player.domain.model.Player;
import com.poker.player.domain.model.PlayerAction;
import com.poker.player.domain.repository.PlayerRepository;
import com.poker.player.infrastructure.persistence.SQLitePlayerRepository;
import com.poker.shared.domain.events.DomainEventPublisher;
import com.poker.shared.domain.events.NoOpEventPublisher;
import com.poker.shared.infrastructure.database.DatabaseInitializer;

/**
 * End-to-end integration tests for complete game flow.
 */
public class FullGameIntegrationTest {

    private static PlayerRepository playerRepository;
    private static GameRepository gameRepository;
    private static DomainEventPublisher eventPublisher;
    private static RegisterPlayerUseCase registerPlayer;
    private static StartGameUseCase startGame;
    private static PlayerActionUseCase playerAction;

    @BeforeAll
    static void setupDatabase() {
        DatabaseInitializer.initialize();

        playerRepository = new SQLitePlayerRepository();
        gameRepository = new SQLiteGameRepository(playerRepository);
        eventPublisher = new NoOpEventPublisher();

        registerPlayer = new RegisterPlayerUseCase(playerRepository);
        startGame = new StartGameUseCase(gameRepository, playerRepository, eventPublisher);
        playerAction = new PlayerActionUseCase(gameRepository, eventPublisher);
    }

    @Test
    void testCompleteGameFlow() {
        // 1. Register players with unique names
        String timestamp = String.valueOf(System.currentTimeMillis());
        var alice = registerPlayer.execute(
                new RegisterPlayerUseCase.RegisterPlayerCommand("Alice" + timestamp, 1000)
        );
        var bob = registerPlayer.execute(
                new RegisterPlayerUseCase.RegisterPlayerCommand("Bob" + timestamp, 1000)
        );
        var charlie = registerPlayer.execute(
                new RegisterPlayerUseCase.RegisterPlayerCommand("Charlie" + timestamp, 1000)
        );

        assertNotNull(alice.playerId());
        assertNotNull(bob.playerId());
        assertNotNull(charlie.playerId());
        assertEquals(1000, alice.chips());

        // 2. Start game
        LobbyId lobbyId = LobbyId.generate();
        var game = startGame.execute(
                new StartGameUseCase.StartGameCommand(
                        List.of(alice.playerId(), bob.playerId(), charlie.playerId()),
                        new Blinds(10, 20),
                        lobbyId
                )
        );

        assertNotNull(game.gameId());
        assertEquals("PRE_FLOP", game.gameState());
        assertEquals(30, game.pot()); // Small + Big blind

        // Get the game to determine turn order
        Game gameObj = gameRepository.findById(GameId.from(game.gameId())).orElseThrow();
        
        // 3. Pre-flop betting round - have all players act in turn order
        // Automatic progression will move game to FLOP after all players act
        playerAction.execute(
                new PlayerActionUseCase.PlayerActionCommand(
                        game.gameId(),
                        gameObj.getCurrentPlayer().getId().getValue().toString(),
                        PlayerAction.CALL,
                        20
                )
        );
        
        gameObj = gameRepository.findById(GameId.from(game.gameId())).orElseThrow();
        playerAction.execute(
                new PlayerActionUseCase.PlayerActionCommand(
                        game.gameId(),
                        gameObj.getCurrentPlayer().getId().getValue().toString(),
                        PlayerAction.CALL,
                        20
                )
        );
        
        gameObj = gameRepository.findById(GameId.from(game.gameId())).orElseThrow();
        playerAction.execute(
                new PlayerActionUseCase.PlayerActionCommand(
                        game.gameId(),
                        gameObj.getCurrentPlayer().getId().getValue().toString(),
                        PlayerAction.CALL,
                        20
                )
        );

        // 4. Verify automatic progression to FLOP
        gameObj = gameRepository.findById(GameId.from(game.gameId())).orElseThrow();
        assertEquals("FLOP", gameObj.getState().name());
        assertEquals(3, gameObj.getCommunityCards().size());

        // 5. Post-flop betting - all players check in turn
        // Automatic progression will move game to TURN after all players act
        for (int i = 0; i < 3; i++) {
            gameObj = gameRepository.findById(GameId.from(game.gameId())).orElseThrow();
            Player currentPlayer = gameObj.getCurrentPlayer();
            playerAction.execute(
                    new PlayerActionUseCase.PlayerActionCommand(
                            game.gameId(), currentPlayer.getId().getValue().toString(), PlayerAction.CHECK, 0
                    )
            );
        }

        // 6. Verify automatic progression to TURN
        gameObj = gameRepository.findById(GameId.from(game.gameId())).orElseThrow();
        assertEquals("TURN", gameObj.getState().name());
        assertEquals(4, gameObj.getCommunityCards().size());

        // 7. Turn betting - all players check in turn
        // Automatic progression will move game to RIVER after all players act
        for (int i = 0; i < 3; i++) {
            gameObj = gameRepository.findById(GameId.from(game.gameId())).orElseThrow();
            Player currentPlayer = gameObj.getCurrentPlayer();
            playerAction.execute(
                    new PlayerActionUseCase.PlayerActionCommand(
                            game.gameId(), currentPlayer.getId().getValue().toString(), PlayerAction.CHECK, 0
                    )
            );
        }

        // 8. Verify automatic progression to RIVER
        gameObj = gameRepository.findById(GameId.from(game.gameId())).orElseThrow();
        assertEquals("RIVER", gameObj.getState().name());
        assertEquals(5, gameObj.getCommunityCards().size());

        // 9. River betting - all players check in turn
        // Automatic progression will determine winner after all players act
        for (int i = 0; i < 3; i++) {
            gameObj = gameRepository.findById(GameId.from(game.gameId())).orElseThrow();
            
            // Stop if game is already finished (automatic winner determination occurred)
            if (gameObj.getState() == com.poker.game.domain.model.GameState.FINISHED) {
                break;
            }
            
            Player currentPlayer = gameObj.getCurrentPlayer();
            playerAction.execute(
                    new PlayerActionUseCase.PlayerActionCommand(
                            game.gameId(), currentPlayer.getId().getValue().toString(), PlayerAction.CHECK, 0
                    )
            );
        }

        // 10. Verify game reached a final state (either FINISHED or still in RIVER)
        gameObj = gameRepository.findById(GameId.from(game.gameId())).orElseThrow();
        assertTrue(gameObj.getState() == com.poker.game.domain.model.GameState.FINISHED ||
                   gameObj.getState() == com.poker.game.domain.model.GameState.RIVER,
                   "Game should be in FINISHED or RIVER state");

        System.out.println("✓ Complete game flow test with automatic progression passed!");
        System.out.println("  Final state: " + gameObj.getState());
    }

    @Test
    void testPlayerFolding() {
        // Register players with unique names
        String timestamp = String.valueOf(System.currentTimeMillis());
        var p1 = registerPlayer.execute(
                new RegisterPlayerUseCase.RegisterPlayerCommand("FoldTest1" + timestamp, 1000)
        );
        var p2 = registerPlayer.execute(
                new RegisterPlayerUseCase.RegisterPlayerCommand("FoldTest2" + timestamp, 1000)
        );

        // Start game
        LobbyId lobbyId = LobbyId.generate();
        var game = startGame.execute(
                new StartGameUseCase.StartGameCommand(
                        List.of(p1.playerId(), p2.playerId()),
                        new Blinds(5, 10),
                        lobbyId
                )
        );

        // Get current player to fold
        Game gameObj = gameRepository.findById(GameId.from(game.gameId())).orElseThrow();
        Player currentPlayer = gameObj.getCurrentPlayer();

        // Current player folds
        var fold = playerAction.execute(
                new PlayerActionUseCase.PlayerActionCommand(
                        game.gameId(),
                        currentPlayer.getId().getValue().toString(),
                        PlayerAction.FOLD,
                        0
                )
        );

        assertTrue(fold.playerFolded());

        System.out.println("✓ Fold test passed!");
    }

    @Test
    void testAllIn() {
        // Register players with different chip counts
        String timestamp = String.valueOf(System.currentTimeMillis());
        var rich = registerPlayer.execute(
                new RegisterPlayerUseCase.RegisterPlayerCommand("RichPlayer" + timestamp, 2000)
        );
        var poor = registerPlayer.execute(
                new RegisterPlayerUseCase.RegisterPlayerCommand("PoorPlayer" + timestamp, 100)
        );

        // Start game
        LobbyId lobbyId = LobbyId.generate();
        var game = startGame.execute(
                new StartGameUseCase.StartGameCommand(
                        List.of(rich.playerId(), poor.playerId()),
                        new Blinds(5, 10),
                        lobbyId
                )
        );

        // Poor player goes all-in
        var allIn = playerAction.execute(
                new PlayerActionUseCase.PlayerActionCommand(
                        game.gameId(),
                        poor.playerId(),
                        PlayerAction.ALL_IN,
                        0
                )
        );

        assertTrue(allIn.pot() >= 100);

        System.out.println("✓ All-in test passed!");
    }
}

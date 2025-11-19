package com.poker.integration;

import com.poker.game.application.*;
import com.poker.game.domain.model.*;
import com.poker.game.domain.repository.GameRepository;
import com.poker.game.infrastructure.persistence.SQLiteGameRepository;
import com.poker.player.application.*;
import com.poker.player.domain.model.PlayerAction;
import com.poker.player.domain.repository.PlayerRepository;
import com.poker.player.infrastructure.persistence.SQLitePlayerRepository;
import com.poker.shared.infrastructure.database.DatabaseInitializer;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.List;

/**
 * End-to-end integration tests for complete game flow.
 */
public class FullGameIntegrationTest {
    
    private static PlayerRepository playerRepository;
    private static GameRepository gameRepository;
    private static RegisterPlayerUseCase registerPlayer;
    private static StartGameUseCase startGame;
    private static PlayerActionUseCase playerAction;
    private static DealCardsUseCase dealCards;
    private static DetermineWinnerUseCase determineWinner;

    @BeforeClass
    public static void setupDatabase() {
        DatabaseInitializer.initialize();
        
        playerRepository = new SQLitePlayerRepository();
        gameRepository = new SQLiteGameRepository(playerRepository);
        
        registerPlayer = new RegisterPlayerUseCase(playerRepository);
        startGame = new StartGameUseCase(gameRepository, playerRepository);
        playerAction = new PlayerActionUseCase(gameRepository);
        dealCards = new DealCardsUseCase(gameRepository);
        determineWinner = new DetermineWinnerUseCase(gameRepository, playerRepository);
    }

    @Test
    public void testCompleteGameFlow() {
        // 1. Register players
        var alice = registerPlayer.execute(
            new RegisterPlayerUseCase.RegisterPlayerCommand("Alice", 1000)
        );
        var bob = registerPlayer.execute(
            new RegisterPlayerUseCase.RegisterPlayerCommand("Bob", 1000)
        );
        var charlie = registerPlayer.execute(
            new RegisterPlayerUseCase.RegisterPlayerCommand("Charlie", 1000)
        );

        assertNotNull(alice.id());
        assertNotNull(bob.id());
        assertNotNull(charlie.id());
        assertEquals(1000, alice.chips());

        // 2. Start game
        var game = startGame.execute(
            new StartGameUseCase.StartGameCommand(
                List.of(alice.id(), bob.id(), charlie.id()),
                new Blinds(10, 20)
            )
        );

        assertNotNull(game.gameId());
        assertEquals("PRE_FLOP", game.state());
        assertEquals(30, game.pot()); // Small + Big blind

        // 3. Pre-flop betting round
        // Charlie calls
        var action1 = playerAction.execute(
            new PlayerActionUseCase.PlayerActionCommand(
                game.gameId(),
                charlie.id(),
                PlayerAction.CALL,
                20
            )
        );
        assertEquals(50, action1.pot());

        // Alice calls
        var action2 = playerAction.execute(
            new PlayerActionUseCase.PlayerActionCommand(
                game.gameId(),
                alice.id(),
                PlayerAction.CALL,
                20
            )
        );
        assertEquals(60, action2.pot());

        // Bob checks
        var action3 = playerAction.execute(
            new PlayerActionUseCase.PlayerActionCommand(
                game.gameId(),
                bob.id(),
                PlayerAction.CHECK,
                0
            )
        );

        // 4. Deal Flop
        var flop = dealCards.dealFlop(
            new DealCardsUseCase.DealCardsCommand(game.gameId())
        );
        assertEquals("FLOP", flop.state());
        assertEquals(3, flop.communityCardsCount());

        // 5. Post-flop betting
        // All players check
        playerAction.execute(
            new PlayerActionUseCase.PlayerActionCommand(
                game.gameId(), alice.id(), PlayerAction.CHECK, 0
            )
        );
        playerAction.execute(
            new PlayerActionUseCase.PlayerActionCommand(
                game.gameId(), bob.id(), PlayerAction.CHECK, 0
            )
        );
        playerAction.execute(
            new PlayerActionUseCase.PlayerActionCommand(
                game.gameId(), charlie.id(), PlayerAction.CHECK, 0
            )
        );

        // 6. Deal Turn
        var turn = dealCards.dealTurn(
            new DealCardsUseCase.DealCardsCommand(game.gameId())
        );
        assertEquals("TURN", turn.state());
        assertEquals(4, turn.communityCardsCount());

        // 7. Deal River
        var river = dealCards.dealRiver(
            new DealCardsUseCase.DealCardsCommand(game.gameId())
        );
        assertEquals("RIVER", river.state());
        assertEquals(5, river.communityCardsCount());

        // 8. Showdown
        var winner = determineWinner.execute(
            new DetermineWinnerUseCase.DetermineWinnerCommand(game.gameId())
        );

        assertNotNull(winner.winnerName());
        assertTrue(winner.totalChips() > 1000 || winner.totalChips() < 1000);
        
        System.out.println("✓ Complete game flow test passed!");
        System.out.println("  Winner: " + winner.winnerName());
        System.out.println("  Pot won: " + winner.potWon());
    }

    @Test
    public void testPlayerFolding() {
        // Register players
        var p1 = registerPlayer.execute(
            new RegisterPlayerUseCase.RegisterPlayerCommand("FoldTest1", 1000)
        );
        var p2 = registerPlayer.execute(
            new RegisterPlayerUseCase.RegisterPlayerCommand("FoldTest2", 1000)
        );

        // Start game
        var game = startGame.execute(
            new StartGameUseCase.StartGameCommand(
                List.of(p1.id(), p2.id()),
                new Blinds(5, 10)
            )
        );

        // Player 1 folds
        var fold = playerAction.execute(
            new PlayerActionUseCase.PlayerActionCommand(
                game.gameId(),
                p1.id(),
                PlayerAction.FOLD,
                0
            )
        );

        assertTrue(fold.playerFolded());
        
        System.out.println("✓ Fold test passed!");
    }

    @Test
    public void testAllIn() {
        // Register players with different chip amounts
        var rich = registerPlayer.execute(
            new RegisterPlayerUseCase.RegisterPlayerCommand("RichPlayer", 2000)
        );
        var poor = registerPlayer.execute(
            new RegisterPlayerUseCase.RegisterPlayerCommand("PoorPlayer", 100)
        );

        // Start game
        var game = startGame.execute(
            new StartGameUseCase.StartGameCommand(
                List.of(rich.id(), poor.id()),
                new Blinds(5, 10)
            )
        );

        // Poor player goes all-in
        var allIn = playerAction.execute(
            new PlayerActionUseCase.PlayerActionCommand(
                game.gameId(),
                poor.id(),
                PlayerAction.ALL_IN,
                0
            )
        );

        assertTrue(allIn.pot() >= 100);
        
        System.out.println("✓ All-in test passed!");
    }
}

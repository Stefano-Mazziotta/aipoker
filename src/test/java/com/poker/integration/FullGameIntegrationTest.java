package com.poker.integration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.poker.game.application.DealCardsUseCase;
import com.poker.game.application.DetermineWinnerUseCase;
import com.poker.game.application.PlayerActionUseCase;
import com.poker.game.application.StartGameUseCase;
import com.poker.game.domain.model.Blinds;
import com.poker.game.domain.model.Game;
import com.poker.game.domain.model.GameId;
import com.poker.game.domain.repository.GameRepository;
import com.poker.game.infrastructure.persistence.SQLiteGameRepository;
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
    private static DealCardsUseCase dealCards;
    private static DetermineWinnerUseCase determineWinner;

    @BeforeAll
    static void setupDatabase() {
        DatabaseInitializer.initialize();

        playerRepository = new SQLitePlayerRepository();
        gameRepository = new SQLiteGameRepository(playerRepository);
        eventPublisher = new NoOpEventPublisher();

        registerPlayer = new RegisterPlayerUseCase(playerRepository);
        startGame = new StartGameUseCase(gameRepository, playerRepository, eventPublisher);
        playerAction = new PlayerActionUseCase(gameRepository, eventPublisher);
        dealCards = new DealCardsUseCase(gameRepository, eventPublisher);
        determineWinner = new DetermineWinnerUseCase(gameRepository, playerRepository, eventPublisher);
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

        // Get the game to determine turn order
        Game gameObj = gameRepository.findById(GameId.from(game.gameId())).orElseThrow();
        
        // 3. Pre-flop betting round - have all players act in turn order
        // NOTE: Action tracking state (playersActedThisRound) is not persisted,
        // so we get current player from fresh game load each time
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

        // 4. Deal Flop
        var flop = dealCards.dealFlop(
                new DealCardsUseCase.DealCardsCommand(game.gameId())
        );
        assertEquals("FLOP", flop.state());
        assertEquals(3, flop.communityCardsCount());

        // 5. Post-flop betting - all players check in turn
        gameObj = gameRepository.findById(GameId.from(game.gameId())).orElseThrow();
        for (int i = 0; i < 3; i++) {
            Player currentPlayer = gameObj.getCurrentPlayer();
            playerAction.execute(
                    new PlayerActionUseCase.PlayerActionCommand(
                            game.gameId(), currentPlayer.getId().getValue().toString(), PlayerAction.CHECK, 0
                    )
            );
            gameObj = gameRepository.findById(GameId.from(game.gameId())).orElseThrow();
        }

        // 6. Deal Turn
        var turn = dealCards.dealTurn(
                new DealCardsUseCase.DealCardsCommand(game.gameId())
        );
        assertEquals("TURN", turn.state());
        assertEquals(4, turn.communityCardsCount());

        // 7. Turn betting - all players check in turn
        gameObj = gameRepository.findById(GameId.from(game.gameId())).orElseThrow();
        for (int i = 0; i < 3; i++) {
            Player currentPlayer = gameObj.getCurrentPlayer();
            playerAction.execute(
                    new PlayerActionUseCase.PlayerActionCommand(
                            game.gameId(), currentPlayer.getId().getValue().toString(), PlayerAction.CHECK, 0
                    )
            );
            gameObj = gameRepository.findById(GameId.from(game.gameId())).orElseThrow();
        }

        // 8. Deal River
        var river = dealCards.dealRiver(
                new DealCardsUseCase.DealCardsCommand(game.gameId())
        );
        assertEquals("RIVER", river.state());
        assertEquals(5, river.communityCardsCount());

        // 9. River betting - all players check in turn
        gameObj = gameRepository.findById(GameId.from(game.gameId())).orElseThrow();
        for (int i = 0; i < 3; i++) {
            Player currentPlayer = gameObj.getCurrentPlayer();
            playerAction.execute(
                    new PlayerActionUseCase.PlayerActionCommand(
                            game.gameId(), currentPlayer.getId().getValue().toString(), PlayerAction.CHECK, 0
                    )
            );
            gameObj = gameRepository.findById(GameId.from(game.gameId())).orElseThrow();
        }

        // 10. Showdown
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
        var game = startGame.execute(
                new StartGameUseCase.StartGameCommand(
                        List.of(p1.id(), p2.id()),
                        new Blinds(5, 10)
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
        // Register players with different chip amounts and unique names
        String timestamp = String.valueOf(System.currentTimeMillis());
        var rich = registerPlayer.execute(
                new RegisterPlayerUseCase.RegisterPlayerCommand("RichPlayer" + timestamp, 2000)
        );
        var poor = registerPlayer.execute(
                new RegisterPlayerUseCase.RegisterPlayerCommand("PoorPlayer" + timestamp, 100)
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

package com.poker.game.domain.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.poker.player.domain.model.Player;

/**
 * Tests for Game aggregate.
 */
public class GameTest {

    @Test
    void testGameCreation() {
        List<Player> players = Arrays.asList(
                Player.create("Alice", 1000),
                Player.create("Bob", 1000)
        );

        Game game = Game.create(players, new Blinds(10, 20));

        assertNotNull(game.getId());
        assertEquals(GameState.WAITING, game.getState());
        assertEquals(2, game.getPlayers().size());
        assertEquals(10, game.getBlinds().getSmallBlind());
        assertEquals(20, game.getBlinds().getBigBlind());

        System.out.println("✓ Game creation test passed!");
    }

    @Test
    void testGameStart() {
        List<Player> players = Arrays.asList(
                Player.create("Player1", 1000),
                Player.create("Player2", 1000),
                Player.create("Player3", 1000)
        );

        Game game = Game.create(players, new Blinds(5, 10));
        game.start();

        assertEquals(GameState.PRE_FLOP, game.getState());
        assertEquals(15, game.getCurrentPot().getAmount()); // Small + Big blind

        // Check that players received hole cards
        for (Player player : game.getPlayers()) {
            assertEquals(2, player.getHand().getCards().size());
        }

        System.out.println("✓ Game start test passed!");
    }

    @Test
    void testDealCommunityCards() {
        List<Player> players = Arrays.asList(
                Player.create("P1", 1000),
                Player.create("P2", 1000)
        );

        Game game = Game.create(players, new Blinds(10, 20));
        game.start();

        // Complete pre-flop betting round - both players must act and match bets
        // After blinds, current bet is 20 (big blind)
        // Both players need to call 20
        game.getCurrentRound().setPlayerBet(players.get(0), 20);
        game.recordPlayerAction(players.get(0)); // P1 calls
        
        game.getCurrentRound().setPlayerBet(players.get(1), 20);
        game.recordPlayerAction(players.get(1)); // P2 calls

        // Deal Flop
        game.dealFlop();
        assertEquals(GameState.FLOP, game.getState());
        assertEquals(3, game.getCommunityCards().size());

        // Complete flop betting round - both check (bet 0 since startNewBettingRound was called)
        game.recordPlayerAction(players.get(0));
        game.recordPlayerAction(players.get(1));

        // Deal Turn
        game.dealTurn();
        assertEquals(GameState.TURN, game.getState());
        assertEquals(4, game.getCommunityCards().size());

        // Complete turn betting round - both check
        game.recordPlayerAction(players.get(0));
        game.recordPlayerAction(players.get(1));

        // Deal River
        game.dealRiver();
        assertEquals(GameState.RIVER, game.getState());
        assertEquals(5, game.getCommunityCards().size());

        System.out.println("✓ Deal community cards test passed!");
    }

    @Test
    void testGameRequiresMinimumPlayers() {
        List<Player> players = Arrays.asList(
                Player.create("LonelyPlayer", 1000)
        );

        // Should throw exception - need at least 2 players
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Game.create(players, new Blinds(10, 20));
        });
        assertNotNull(exception);
    }

    @Test
    void testGameRequiresMaximumPlayers() {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            players.add(Player.create("Player" + i, 1000));
        }

        // Should throw exception - max 9 players
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Game.create(players, new Blinds(10, 20));
        });
        assertNotNull(exception);
    }

    @Test
    void testDealerRotation() {
        List<Player> players = Arrays.asList(
                Player.create("P1", 1000),
                Player.create("P2", 1000),
                Player.create("P3", 1000)
        );

        Game game = Game.create(players, new Blinds(10, 20));

        assertEquals(0, game.getDealerPosition());

        game.advanceDealer();
        assertEquals(1, game.getDealerPosition());

        game.advanceDealer();
        assertEquals(2, game.getDealerPosition());

        game.advanceDealer();
        assertEquals(0, game.getDealerPosition()); // Should wrap around

        System.out.println("✓ Dealer rotation test passed!");
    }
}

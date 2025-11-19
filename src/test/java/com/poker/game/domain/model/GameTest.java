package com.poker.game.domain.model;

import com.poker.player.domain.model.*;
import com.poker.shared.domain.valueobject.*;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;

/**
 * Tests for Game aggregate.
 */
public class GameTest {

    @Test
    public void testGameCreation() {
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
    public void testGameStart() {
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
    public void testDealCommunityCards() {
        List<Player> players = Arrays.asList(
            Player.create("P1", 1000),
            Player.create("P2", 1000)
        );

        Game game = Game.create(players, new Blinds(10, 20));
        game.start();

        // Deal Flop
        game.dealFlop();
        assertEquals(GameState.FLOP, game.getState());
        assertEquals(3, game.getCommunityCards().size());

        // Deal Turn
        game.dealTurn();
        assertEquals(GameState.TURN, game.getState());
        assertEquals(4, game.getCommunityCards().size());

        // Deal River
        game.dealRiver();
        assertEquals(GameState.RIVER, game.getState());
        assertEquals(5, game.getCommunityCards().size());
        
        System.out.println("✓ Deal community cards test passed!");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGameRequiresMinimumPlayers() {
        List<Player> players = Arrays.asList(
            Player.create("LonelyPlayer", 1000)
        );

        // Should throw exception - need at least 2 players
        Game.create(players, new Blinds(10, 20));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGameRequiresMaximumPlayers() {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            players.add(Player.create("Player" + i, 1000));
        }

        // Should throw exception - max 9 players
        Game.create(players, new Blinds(10, 20));
    }

    @Test
    public void testDealerRotation() {
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

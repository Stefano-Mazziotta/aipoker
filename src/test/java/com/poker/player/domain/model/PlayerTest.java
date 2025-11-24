package com.poker.player.domain.model;

import com.poker.shared.domain.valueobject.Card;
import com.poker.shared.domain.valueobject.Rank;
import com.poker.shared.domain.valueobject.Suit;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Player aggregate.
 */
class PlayerTest {

    @Test
    void testCreatePlayer() {
        Player player = Player.create("Alice", 1000);

        assertNotNull(player.getId());
        assertEquals("Alice", player.getName());
        assertEquals(1000, player.getChipsAmount());
        assertFalse(player.isFolded());
    }

    @Test
    void testReceiveCard() {
        Player player = Player.create("Bob", 500);
        Card card = new Card(Rank.ACE, Suit.SPADES);
        
        player.receiveCard(card);
        
        assertEquals(1, player.getHand().getCards().size());
        assertEquals(card, player.getHand().getCards().getFirst());
    }

    @Test
    void testSubtractChips() {
        Player player = Player.create("Charlie", 1000);
        
        player.subtractChips(300);
        
        assertEquals(700, player.getChipsAmount());
    }

    @Test
    void testSubtractChipsThrowsOnInsufficientFunds() {
        Player player = Player.create("Dave", 100);
        
        assertThrows(Exception.class, () -> player.subtractChips(200));
    }

    @Test
    void testAddChips() {
        Player player = Player.create("Eve", 500);
        
        player.addChips(500);
        
        assertEquals(1000, player.getChipsAmount());
    }

    @Test
    void testFold() {
        Player player = Player.create("Frank", 1000);
        
        player.fold();
        
        assertTrue(player.isFolded());
    }

    @Test
    void testResetFoldedStatus() {
        Player player = Player.create("Grace", 1000);
        player.fold();
        
        player.resetFoldedStatus();
        
        assertFalse(player.isFolded());
    }

    @Test
    void testCanAfford() {
        Player player = Player.create("Henry", 1000);
        
        assertTrue(player.canAfford(500));
        assertTrue(player.canAfford(1000));
        assertFalse(player.canAfford(1001));
    }

    @Test
    void testClearHand() {
        Player player = Player.create("Ivy", 1000);
        player.receiveCard(new Card(Rank.KING, Suit.HEARTS));
        player.receiveCard(new Card(Rank.QUEEN, Suit.HEARTS));
        
        player.clearHand();
        
        assertEquals(0, player.getHand().getCards().size());
    }

    @Test
    void testReconstitute() {
        PlayerId id = PlayerId.generate();
        Player player = Player.reconstitute(id, "Jack", 750, true);
        
        assertEquals(id, player.getId());
        assertEquals("Jack", player.getName());
        assertEquals(750, player.getChipsAmount());
        assertTrue(player.isFolded());
    }
}

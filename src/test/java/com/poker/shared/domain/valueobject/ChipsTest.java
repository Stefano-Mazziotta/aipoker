package com.poker.shared.domain.valueobject;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Chips value object.
 */
class ChipsTest {

    @Test
    void testCreateChips() {
        Chips chips = Chips.of(1000);
        
        assertEquals(1000, chips.getAmount());
    }

    @Test
    void testCreateChipsThrowsOnNegative() {
        assertThrows(Exception.class, () -> Chips.of(-100));
    }

    @Test
    void testAdd() {
        Chips chips = Chips.of(500);
        Chips result = chips.add(300);
        
        assertEquals(800, result.getAmount());
        assertEquals(500, chips.getAmount()); // Original unchanged (immutable)
    }

    @Test
    void testSubtract() {
        Chips chips = Chips.of(1000);
        Chips result = chips.subtract(400);
        
        assertEquals(600, result.getAmount());
        assertEquals(1000, chips.getAmount()); // Original unchanged
    }

    @Test
    void testSubtractThrowsOnInsufficientFunds() {
        Chips chips = Chips.of(100);
        
        assertThrows(Exception.class, () -> chips.subtract(200));
    }

    @Test
    void testCanAfford() {
        Chips chips = Chips.of(1000);
        
        assertTrue(chips.canAfford(500));
        assertTrue(chips.canAfford(1000));
        assertFalse(chips.canAfford(1001));
    }

    @Test
    void testEquality() {
        Chips chips1 = Chips.of(500);
        Chips chips2 = Chips.of(500);
        Chips chips3 = Chips.of(600);
        
        assertEquals(chips1, chips2);
        assertNotEquals(chips1, chips3);
    }
}

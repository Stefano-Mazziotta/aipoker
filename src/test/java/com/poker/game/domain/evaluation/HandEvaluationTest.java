package com.poker.game.domain.evaluation;

import com.poker.shared.domain.valueobject.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for hand evaluation system.
 */
class HandEvaluationTest {

    private final HandEvaluationStrategy evaluator = new TexasHoldemEvaluator();

    @Test
    void testStraightFlushDetection() {
        List<Card> cards = List.of(
            new Card(Rank.TEN, Suit.HEARTS),
            new Card(Rank.JACK, Suit.HEARTS),
            new Card(Rank.QUEEN, Suit.HEARTS),
            new Card(Rank.KING, Suit.HEARTS),
            new Card(Rank.ACE, Suit.HEARTS),
            new Card(Rank.TWO, Suit.CLUBS),
            new Card(Rank.THREE, Suit.DIAMONDS)
        );

        PokerHand hand = evaluator.evaluate(cards);
        
        assertEquals(HandRank.STRAIGHT_FLUSH, hand.getRank());
    }

    @Test
    void testFourOfAKindDetection() {
        List<Card> cards = List.of(
            new Card(Rank.KING, Suit.HEARTS),
            new Card(Rank.KING, Suit.DIAMONDS),
            new Card(Rank.KING, Suit.CLUBS),
            new Card(Rank.KING, Suit.SPADES),
            new Card(Rank.ACE, Suit.HEARTS),
            new Card(Rank.TWO, Suit.CLUBS),
            new Card(Rank.THREE, Suit.DIAMONDS)
        );

        PokerHand hand = evaluator.evaluate(cards);
        
        assertEquals(HandRank.FOUR_OF_A_KIND, hand.getRank());
    }

    @Test
    void testFullHouseDetection() {
        List<Card> cards = List.of(
            new Card(Rank.KING, Suit.HEARTS),
            new Card(Rank.KING, Suit.DIAMONDS),
            new Card(Rank.KING, Suit.CLUBS),
            new Card(Rank.ACE, Suit.SPADES),
            new Card(Rank.ACE, Suit.HEARTS),
            new Card(Rank.TWO, Suit.CLUBS),
            new Card(Rank.THREE, Suit.DIAMONDS)
        );

        PokerHand hand = evaluator.evaluate(cards);
        
        assertEquals(HandRank.FULL_HOUSE, hand.getRank());
    }

    @Test
    void testFlushDetection() {
        List<Card> cards = List.of(
            new Card(Rank.TWO, Suit.HEARTS),
            new Card(Rank.FIVE, Suit.HEARTS),
            new Card(Rank.SEVEN, Suit.HEARTS),
            new Card(Rank.NINE, Suit.HEARTS),
            new Card(Rank.KING, Suit.HEARTS),
            new Card(Rank.ACE, Suit.CLUBS),
            new Card(Rank.THREE, Suit.DIAMONDS)
        );

        PokerHand hand = evaluator.evaluate(cards);
        
        assertEquals(HandRank.FLUSH, hand.getRank());
    }

    @Test
    void testStraightDetection() {
        List<Card> cards = List.of(
            new Card(Rank.FIVE, Suit.HEARTS),
            new Card(Rank.SIX, Suit.DIAMONDS),
            new Card(Rank.SEVEN, Suit.CLUBS),
            new Card(Rank.EIGHT, Suit.SPADES),
            new Card(Rank.NINE, Suit.HEARTS),
            new Card(Rank.ACE, Suit.CLUBS),
            new Card(Rank.TWO, Suit.DIAMONDS)
        );

        PokerHand hand = evaluator.evaluate(cards);
        
        assertEquals(HandRank.STRAIGHT, hand.getRank());
    }

    @Test
    void testAceLowStraightDetection() {
        List<Card> cards = List.of(
            new Card(Rank.ACE, Suit.HEARTS),
            new Card(Rank.TWO, Suit.DIAMONDS),
            new Card(Rank.THREE, Suit.CLUBS),
            new Card(Rank.FOUR, Suit.SPADES),
            new Card(Rank.FIVE, Suit.HEARTS),
            new Card(Rank.KING, Suit.CLUBS),
            new Card(Rank.QUEEN, Suit.DIAMONDS)
        );

        PokerHand hand = evaluator.evaluate(cards);
        
        assertEquals(HandRank.STRAIGHT, hand.getRank());
    }

    @Test
    void testThreeOfAKindDetection() {
        List<Card> cards = List.of(
            new Card(Rank.KING, Suit.HEARTS),
            new Card(Rank.KING, Suit.DIAMONDS),
            new Card(Rank.KING, Suit.CLUBS),
            new Card(Rank.ACE, Suit.SPADES),
            new Card(Rank.QUEEN, Suit.HEARTS),
            new Card(Rank.TWO, Suit.CLUBS),
            new Card(Rank.THREE, Suit.DIAMONDS)
        );

        PokerHand hand = evaluator.evaluate(cards);
        
        assertEquals(HandRank.THREE_OF_A_KIND, hand.getRank());
    }

    @Test
    void testTwoPairDetection() {
        List<Card> cards = List.of(
            new Card(Rank.KING, Suit.HEARTS),
            new Card(Rank.KING, Suit.DIAMONDS),
            new Card(Rank.QUEEN, Suit.CLUBS),
            new Card(Rank.QUEEN, Suit.SPADES),
            new Card(Rank.ACE, Suit.HEARTS),
            new Card(Rank.TWO, Suit.CLUBS),
            new Card(Rank.THREE, Suit.DIAMONDS)
        );

        PokerHand hand = evaluator.evaluate(cards);
        
        assertEquals(HandRank.TWO_PAIR, hand.getRank());
    }

    @Test
    void testOnePairDetection() {
        List<Card> cards = List.of(
            new Card(Rank.KING, Suit.HEARTS),
            new Card(Rank.KING, Suit.DIAMONDS),
            new Card(Rank.ACE, Suit.CLUBS),
            new Card(Rank.QUEEN, Suit.SPADES),
            new Card(Rank.JACK, Suit.HEARTS),
            new Card(Rank.TWO, Suit.CLUBS),
            new Card(Rank.THREE, Suit.DIAMONDS)
        );

        PokerHand hand = evaluator.evaluate(cards);
        
        assertEquals(HandRank.ONE_PAIR, hand.getRank());
    }

    @Test
    void testHighCardDetection() {
        List<Card> cards = List.of(
            new Card(Rank.ACE, Suit.HEARTS),
            new Card(Rank.KING, Suit.DIAMONDS),
            new Card(Rank.QUEEN, Suit.CLUBS),
            new Card(Rank.JACK, Suit.SPADES),
            new Card(Rank.NINE, Suit.HEARTS),
            new Card(Rank.TWO, Suit.CLUBS),
            new Card(Rank.THREE, Suit.DIAMONDS)
        );

        PokerHand hand = evaluator.evaluate(cards);
        
        assertEquals(HandRank.HIGH_CARD, hand.getRank());
    }

    @Test
    void testHandComparison() {
        List<Card> straightFlushCards = List.of(
            new Card(Rank.NINE, Suit.HEARTS),
            new Card(Rank.TEN, Suit.HEARTS),
            new Card(Rank.JACK, Suit.HEARTS),
            new Card(Rank.QUEEN, Suit.HEARTS),
            new Card(Rank.KING, Suit.HEARTS),
            new Card(Rank.TWO, Suit.CLUBS),
            new Card(Rank.THREE, Suit.DIAMONDS)
        );

        List<Card> fourOfAKindCards = List.of(
            new Card(Rank.ACE, Suit.HEARTS),
            new Card(Rank.ACE, Suit.DIAMONDS),
            new Card(Rank.ACE, Suit.CLUBS),
            new Card(Rank.ACE, Suit.SPADES),
            new Card(Rank.KING, Suit.HEARTS),
            new Card(Rank.TWO, Suit.CLUBS),
            new Card(Rank.THREE, Suit.DIAMONDS)
        );

        PokerHand straightFlush = evaluator.evaluate(straightFlushCards);
        PokerHand fourOfAKind = evaluator.evaluate(fourOfAKindCards);
        
        assertTrue(straightFlush.compareTo(fourOfAKind) > 0);
    }
}

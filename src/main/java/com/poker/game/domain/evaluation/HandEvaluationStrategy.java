package com.poker.game.domain.evaluation;

import com.poker.shared.domain.valueobject.Card;
import java.util.List;

/**
 * Strategy interface for evaluating poker hands.
 * Allows for different evaluation strategies (Texas Hold'em, Omaha, etc.)
 * 
 * REUSED from existing implementation.
 */
public interface HandEvaluationStrategy {
    /**
     * Evaluates a list of cards and returns the best poker hand.
     * 
     * @param cards The cards to evaluate (typically 7 cards for Texas Hold'em)
     * @return The best poker hand found
     */
    PokerHand evaluate(List<Card> cards);
}

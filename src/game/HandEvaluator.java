package game;

import cards.Card;
import game.evaluation.PokerHand;
import game.evaluation.TexasHoldemEvaluator;
import java.util.*;

/**
 * Facade class for hand evaluation in the game package.
 * Delegates to the new evaluation module.
 */
public class HandEvaluator {
    private static final TexasHoldemEvaluator evaluator = new TexasHoldemEvaluator();
    
    public static int evaluateBestHand(List<Card> holeCards, List<Card> communityCards) {
        List<Card> allCards = new ArrayList<>(holeCards);
        allCards.addAll(communityCards);
        
        if (allCards.size() != 7) {
            throw new IllegalArgumentException("Expected 7 cards for evaluation");
        }
        
        PokerHand bestHand = evaluator.evaluate(allCards);
        
        // Return a numeric score: rank value * 1000 + high card value
        return bestHand.getRank().getValue() * 1000 + 
               bestHand.getCards().get(0).getRank().ordinal();
    }
    
    /**
     * Returns the best poker hand for the given cards.
     */
    public static PokerHand evaluate(List<Card> holeCards, List<Card> communityCards) {
        List<Card> allCards = new ArrayList<>(holeCards);
        allCards.addAll(communityCards);
        return evaluator.evaluate(allCards);
    }
}

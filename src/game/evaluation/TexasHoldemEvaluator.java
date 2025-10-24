package game.evaluation;

import cards.Card;
import game.evaluation.detectors.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Texas Hold'em specific hand evaluator.
 * Uses a chain of responsibility pattern with hand detectors to find the best 5-card hand.
 */
public class TexasHoldemEvaluator implements HandEvaluationStrategy {
    
    private final List<HandDetector> detectors;

    public TexasHoldemEvaluator() {
        // Initialize detectors in descending order of hand strength
        this.detectors = new ArrayList<>();
        detectors.add(new StraightFlushDetector());
        detectors.add(new FourOfAKindDetector());
        detectors.add(new FullHouseDetector());
        detectors.add(new FlushDetector());
        detectors.add(new StraightDetector());
        detectors.add(new ThreeOfAKindDetector());
        detectors.add(new TwoPairDetector());
        detectors.add(new OnePairDetector());
        detectors.add(new HighCardDetector());
    }

    /**
     * Constructor that accepts custom detectors (for testing or extensibility).
     */
    public TexasHoldemEvaluator(List<HandDetector> detectors) {
        this.detectors = new ArrayList<>(detectors);
    }

    @Override
    public PokerHand evaluate(List<Card> cards) {
        if (cards.size() != 7) {
            throw new IllegalArgumentException("Texas Hold'em requires exactly 7 cards (2 hole + 5 community)");
        }

        // Generate all possible 5-card combinations
        List<List<Card>> combinations = CombinationUtils.generateFiveCardCombinations(cards);

        PokerHand bestHand = null;

        // Evaluate each combination
        for (List<Card> combination : combinations) {
            PokerHand hand = evaluateFiveCards(combination);
            
            if (bestHand == null || hand.compareTo(bestHand) > 0) {
                bestHand = hand;
            }
        }

        return bestHand;
    }

    /**
     * Evaluates exactly 5 cards using the chain of detectors.
     */
    private PokerHand evaluateFiveCards(List<Card> fiveCards) {
        for (HandDetector detector : detectors) {
            var result = detector.detect(fiveCards);
            if (result.isPresent()) {
                return result.get();
            }
        }
        
        // This should never happen if HighCardDetector is implemented correctly
        throw new IllegalStateException("No hand detected for: " + fiveCards);
    }
}

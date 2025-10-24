package game.evaluation;

import cards.Card;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for generating combinations of cards.
 * Uses mathematical combination logic to generate all possible 5-card hands from 7 cards.
 */
public class CombinationUtils {
    
    /**
     * Generates all possible 5-card combinations from a list of 7 cards.
     * This produces 7C5 = 21 combinations.
     * 
     * @param cards List of 7 cards
     * @return List of all possible 5-card combinations
     */
    public static List<List<Card>> generateFiveCardCombinations(List<Card> cards) {
        if (cards.size() != 7) {
            throw new IllegalArgumentException("Expected exactly 7 cards, got " + cards.size());
        }
        
        List<List<Card>> combinations = new ArrayList<>();
        generateCombinations(cards, 5, 0, new ArrayList<>(), combinations);
        return combinations;
    }

    /**
     * Recursive helper method to generate combinations.
     */
    private static void generateCombinations(
            List<Card> cards,
            int length,
            int startIndex,
            List<Card> current,
            List<List<Card>> result) {
        
        if (current.size() == length) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = startIndex; i <= cards.size() - (length - current.size()); i++) {
            current.add(cards.get(i));
            generateCombinations(cards, length, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    /**
     * Generates all possible k-element combinations from n elements.
     * 
     * @param cards List of cards to combine
     * @param k Number of cards in each combination
     * @return List of all possible k-card combinations
     */
    public static List<List<Card>> generateCombinations(List<Card> cards, int k) {
        if (k > cards.size()) {
            throw new IllegalArgumentException("k cannot be greater than the number of cards");
        }
        
        List<List<Card>> combinations = new ArrayList<>();
        generateCombinations(cards, k, 0, new ArrayList<>(), combinations);
        return combinations;
    }
}

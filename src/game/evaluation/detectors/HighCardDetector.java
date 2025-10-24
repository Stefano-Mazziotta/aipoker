package game.evaluation.detectors;

import cards.Card;
import cards.Rank;
import game.evaluation.HandDetector;
import game.evaluation.HandRank;
import game.evaluation.PokerHand;
import java.util.*;

/**
 * Detects High Card hands (no other hand type).
 * This is the fallback detector and should always succeed.
 */
public class HighCardDetector implements HandDetector {
    
    @Override
    public Optional<PokerHand> detect(List<Card> cards) {
        if (cards.size() != 5) {
            return Optional.empty();
        }

        // Sort cards by rank (descending)
        List<Card> sortedCards = new ArrayList<>(cards);
        sortedCards.sort((c1, c2) -> Integer.compare(
            getRankValue(c2.getRank()),
            getRankValue(c1.getRank())
        ));

        return Optional.of(new PokerHand(HandRank.HIGH_CARD, sortedCards));
    }

    private int getRankValue(Rank rank) {
        // ACE is highest
        if (rank == Rank.ACE) return 14;
        return rank.ordinal() + 2;
    }
}

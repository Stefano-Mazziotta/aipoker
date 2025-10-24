package game.evaluation.detectors;

import cards.Card;
import cards.Rank;
import cards.Suit;
import game.evaluation.HandDetector;
import game.evaluation.HandRank;
import game.evaluation.PokerHand;
import java.util.*;

/**
 * Detects Flush hands (five cards of the same suit).
 */
public class FlushDetector implements HandDetector {
    
    @Override
    public Optional<PokerHand> detect(List<Card> cards) {
        if (cards.size() != 5) {
            return Optional.empty();
        }

        // Check if all cards have the same suit
        Suit firstSuit = cards.get(0).getSuit();
        boolean allSameSuit = cards.stream()
            .allMatch(card -> card.getSuit() == firstSuit);

        if (!allSameSuit) {
            return Optional.empty();
        }

        // Sort cards by rank (descending)
        List<Card> sortedCards = new ArrayList<>(cards);
        sortedCards.sort((c1, c2) -> Integer.compare(
            getRankValue(c2.getRank()),
            getRankValue(c1.getRank())
        ));

        return Optional.of(new PokerHand(HandRank.FLUSH, sortedCards));
    }

    private int getRankValue(Rank rank) {
        if (rank == Rank.ACE) return 14;
        return rank.ordinal() + 2;
    }
}

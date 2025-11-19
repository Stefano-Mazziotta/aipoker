package com.poker.game.domain.evaluation.detectors;

import com.poker.shared.domain.valueobject.Card;
import com.poker.shared.domain.valueobject.Rank;
import com.poker.game.domain.evaluation.HandDetector;
import com.poker.game.domain.evaluation.HandRank;
import com.poker.game.domain.evaluation.PokerHand;
import java.util.*;

/**
 * Detects High Card hands (no other hand type).
 * This is the fallback detector and should always succeed.
 * 
 * REUSED from existing implementation.
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
        return rank.getValue();
    }
}

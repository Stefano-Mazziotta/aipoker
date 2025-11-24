package com.poker.game.domain.evaluation.detectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.poker.game.domain.evaluation.HandDetector;
import com.poker.game.domain.evaluation.HandRank;
import com.poker.game.domain.evaluation.PokerHand;
import com.poker.shared.domain.valueobject.Card;
import com.poker.shared.domain.valueobject.Suit;

/**
 * Detects Flush hands (five cards of the same suit).
 * REUSED from existing implementation.
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
            c2.getRank().getValue(),
            c1.getRank().getValue()
        ));

        return Optional.of(new PokerHand(HandRank.FLUSH, sortedCards));
    }
}

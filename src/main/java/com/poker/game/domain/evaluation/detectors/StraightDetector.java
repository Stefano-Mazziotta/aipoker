package com.poker.game.domain.evaluation.detectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.poker.game.domain.evaluation.HandDetector;
import com.poker.game.domain.evaluation.HandRank;
import com.poker.game.domain.evaluation.PokerHand;
import com.poker.shared.domain.valueobject.Card;

/**
 * Detects Straight hands (five consecutive ranks).
 * Handles special case: Ace-low straight (A-2-3-4-5).
 * 
 * REUSED from existing implementation.
 */
public class StraightDetector implements HandDetector {
    
    @Override
    public Optional<PokerHand> detect(List<Card> cards) {
        if (cards.size() != 5) {
            return Optional.empty();
        }

        List<Card> sortedCards = new ArrayList<>(cards);
        sortedCards.sort((c1, c2) -> Integer.compare(
            c2.getRank().getValue(),
            c1.getRank().getValue()
        ));

        // Check for standard straight
        if (isConsecutive(sortedCards)) {
            return Optional.of(new PokerHand(HandRank.STRAIGHT, sortedCards));
        }

        // Check for Ace-low straight (A-2-3-4-5)
        if (isAceLowStraight(sortedCards)) {
            // Rearrange: 5-4-3-2-A
            List<Card> aceLowCards = new ArrayList<>();
            for (int i = 1; i < sortedCards.size(); i++) {
                aceLowCards.add(sortedCards.get(i));
            }
            aceLowCards.add(sortedCards.getFirst()); // Move Ace to end
            return Optional.of(new PokerHand(HandRank.STRAIGHT, aceLowCards));
        }

        return Optional.empty();
    }

    private boolean isConsecutive(List<Card> sortedCards) {
        for (int i = 0; i < sortedCards.size() - 1; i++) {
            int currentValue = sortedCards.get(i).getRank().getValue();
            int nextValue = sortedCards.get(i + 1).getRank().getValue();
            if (currentValue - nextValue != 1) {
                return false;
            }
        }
        return true;
    }

    private boolean isAceLowStraight(List<Card> sortedCards) {
        // Check if cards are A-5-4-3-2
        List<Integer> values = new ArrayList<>();
        for (Card card : sortedCards) {
            values.add(card.getRank().getValue());
        }
        
        return values.getFirst() == 14 && // Ace
               values.get(1) == 5 &&
               values.get(2) == 4 &&
               values.get(3) == 3 &&
               values.get(4) == 2;
    }
}

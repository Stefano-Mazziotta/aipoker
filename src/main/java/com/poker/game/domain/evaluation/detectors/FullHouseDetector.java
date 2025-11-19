package com.poker.game.domain.evaluation.detectors;

import com.poker.shared.domain.valueobject.Card;
import com.poker.shared.domain.valueobject.Rank;
import com.poker.game.domain.evaluation.HandDetector;
import com.poker.game.domain.evaluation.HandRank;
import com.poker.game.domain.evaluation.PokerHand;
import java.util.*;

/**
 * Detects Full House hands (three of a kind + pair).
 * REUSED from existing implementation.
 */
public class FullHouseDetector implements HandDetector {
    
    @Override
    public Optional<PokerHand> detect(List<Card> cards) {
        if (cards.size() != 5) {
            return Optional.empty();
        }

        Map<Rank, List<Card>> rankGroups = groupByRank(cards);

        List<Card> threeKind = null;
        List<Card> pair = null;

        // Find three-of-a-kind and pair
        for (List<Card> group : rankGroups.values()) {
            if (group.size() == 3) {
                threeKind = group;
            } else if (group.size() == 2) {
                pair = group;
            }
        }

        if (threeKind == null || pair == null) {
            return Optional.empty();
        }

        // Build final hand: three-of-a-kind first, then pair
        List<Card> handCards = new ArrayList<>(threeKind);
        handCards.addAll(pair);

        return Optional.of(new PokerHand(HandRank.FULL_HOUSE, handCards));
    }

    private Map<Rank, List<Card>> groupByRank(List<Card> cards) {
        Map<Rank, List<Card>> groups = new HashMap<>();
        for (Card card : cards) {
            groups.computeIfAbsent(card.getRank(), k -> new ArrayList<>()).add(card);
        }
        return groups;
    }
}

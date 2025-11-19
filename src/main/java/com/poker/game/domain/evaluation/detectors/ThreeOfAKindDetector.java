package com.poker.game.domain.evaluation.detectors;

import com.poker.shared.domain.valueobject.Card;
import com.poker.shared.domain.valueobject.Rank;
import com.poker.game.domain.evaluation.HandDetector;
import com.poker.game.domain.evaluation.HandRank;
import com.poker.game.domain.evaluation.PokerHand;
import java.util.*;

/**
 * Detects Three of a Kind hands (three cards of the same rank).
 * REUSED from existing implementation.
 */
public class ThreeOfAKindDetector implements HandDetector {
    
    @Override
    public Optional<PokerHand> detect(List<Card> cards) {
        if (cards.size() != 5) {
            return Optional.empty();
        }

        Map<Rank, List<Card>> rankGroups = groupByRank(cards);

        // Find exactly one three-of-a-kind
        List<Card> threeKind = null;
        for (List<Card> group : rankGroups.values()) {
            if (group.size() == 3) {
                threeKind = group;
                break;
            }
        }

        if (threeKind == null) {
            return Optional.empty();
        }

        // Collect kickers
        List<Card> kickers = new ArrayList<>();
        for (Card card : cards) {
            if (!threeKind.contains(card)) {
                kickers.add(card);
            }
        }

        // Sort kickers by rank (descending)
        kickers.sort((c1, c2) -> Integer.compare(
            c2.getRank().getValue(),
            c1.getRank().getValue()
        ));

        // Build final hand: three-of-a-kind first, then kickers
        List<Card> handCards = new ArrayList<>(threeKind);
        handCards.addAll(kickers);

        return Optional.of(new PokerHand(HandRank.THREE_OF_A_KIND, handCards));
    }

    private Map<Rank, List<Card>> groupByRank(List<Card> cards) {
        Map<Rank, List<Card>> groups = new HashMap<>();
        for (Card card : cards) {
            groups.computeIfAbsent(card.getRank(), k -> new ArrayList<>()).add(card);
        }
        return groups;
    }
}

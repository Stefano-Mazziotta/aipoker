package com.poker.game.domain.evaluation.detectors;

import com.poker.shared.domain.valueobject.Card;
import com.poker.shared.domain.valueobject.Rank;
import com.poker.game.domain.evaluation.HandDetector;
import com.poker.game.domain.evaluation.HandRank;
import com.poker.game.domain.evaluation.PokerHand;
import java.util.*;

/**
 * Detects One Pair hands (two cards of the same rank).
 * REUSED from existing implementation.
 */
public class OnePairDetector implements HandDetector {
    
    @Override
    public Optional<PokerHand> detect(List<Card> cards) {
        if (cards.size() != 5) {
            return Optional.empty();
        }

        Map<Rank, List<Card>> rankGroups = groupByRank(cards);

        // Find exactly one pair
        List<Card> pairCards = null;
        for (List<Card> group : rankGroups.values()) {
            if (group.size() == 2) {
                pairCards = group;
                break;
            }
        }

        if (pairCards == null) {
            return Optional.empty();
        }

        // Collect kickers (remaining cards)
        List<Card> kickers = new ArrayList<>();
        for (Card card : cards) {
            if (!pairCards.contains(card)) {
                kickers.add(card);
            }
        }

        // Sort kickers by rank (descending)
        kickers.sort((c1, c2) -> Integer.compare(
            c2.getRank().getValue(),
            c1.getRank().getValue()
        ));

        // Build final hand: pair first, then kickers
        List<Card> handCards = new ArrayList<>(pairCards);
        handCards.addAll(kickers);

        return Optional.of(new PokerHand(HandRank.ONE_PAIR, handCards));
    }

    private Map<Rank, List<Card>> groupByRank(List<Card> cards) {
        Map<Rank, List<Card>> groups = new HashMap<>();
        for (Card card : cards) {
            groups.computeIfAbsent(card.getRank(), k -> new ArrayList<>()).add(card);
        }
        return groups;
    }
}

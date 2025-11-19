package com.poker.game.domain.evaluation.detectors;

import com.poker.shared.domain.valueobject.Card;
import com.poker.shared.domain.valueobject.Rank;
import com.poker.game.domain.evaluation.HandDetector;
import com.poker.game.domain.evaluation.HandRank;
import com.poker.game.domain.evaluation.PokerHand;
import java.util.*;

/**
 * Detects Two Pair hands (two different pairs).
 * REUSED from existing implementation.
 */
public class TwoPairDetector implements HandDetector {
    
    @Override
    public Optional<PokerHand> detect(List<Card> cards) {
        if (cards.size() != 5) {
            return Optional.empty();
        }

        Map<Rank, List<Card>> rankGroups = groupByRank(cards);

        // Find exactly two pairs
        List<List<Card>> pairs = new ArrayList<>();
        for (List<Card> group : rankGroups.values()) {
            if (group.size() == 2) {
                pairs.add(group);
            }
        }

        if (pairs.size() != 2) {
            return Optional.empty();
        }

        // Sort pairs by rank (higher pair first)
        pairs.sort((p1, p2) -> Integer.compare(
            p2.get(0).getRank().getValue(),
            p1.get(0).getRank().getValue()
        ));

        // Find the kicker
        List<Card> kickers = new ArrayList<>();
        for (Card card : cards) {
            boolean isPart = pairs.stream().anyMatch(pair -> pair.contains(card));
            if (!isPart) {
                kickers.add(card);
            }
        }

        // Build final hand: higher pair, lower pair, kicker
        List<Card> handCards = new ArrayList<>();
        handCards.addAll(pairs.get(0));
        handCards.addAll(pairs.get(1));
        handCards.addAll(kickers);

        return Optional.of(new PokerHand(HandRank.TWO_PAIR, handCards));
    }

    private Map<Rank, List<Card>> groupByRank(List<Card> cards) {
        Map<Rank, List<Card>> groups = new HashMap<>();
        for (Card card : cards) {
            groups.computeIfAbsent(card.getRank(), k -> new ArrayList<>()).add(card);
        }
        return groups;
    }
}

package game.evaluation.detectors;

import cards.Card;
import cards.Rank;
import game.evaluation.HandDetector;
import game.evaluation.HandRank;
import game.evaluation.PokerHand;
import java.util.*;

/**
 * Detects Three of a Kind hands (three cards of the same rank).
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
            getRankValue(c2.getRank()),
            getRankValue(c1.getRank())
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

    private int getRankValue(Rank rank) {
        if (rank == Rank.ACE) return 14;
        return rank.ordinal() + 2;
    }
}

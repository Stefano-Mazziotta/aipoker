package game.evaluation.detectors;

import cards.Card;
import cards.Rank;
import game.evaluation.HandDetector;
import game.evaluation.HandRank;
import game.evaluation.PokerHand;
import java.util.*;

/**
 * Detects Four of a Kind hands (four cards of the same rank).
 */
public class FourOfAKindDetector implements HandDetector {
    
    @Override
    public Optional<PokerHand> detect(List<Card> cards) {
        if (cards.size() != 5) {
            return Optional.empty();
        }

        Map<Rank, List<Card>> rankGroups = groupByRank(cards);

        // Find exactly one four-of-a-kind
        List<Card> fourKind = null;
        for (List<Card> group : rankGroups.values()) {
            if (group.size() == 4) {
                fourKind = group;
                break;
            }
        }

        if (fourKind == null) {
            return Optional.empty();
        }

        // Find the kicker
        List<Card> kicker = new ArrayList<>();
        for (Card card : cards) {
            if (!fourKind.contains(card)) {
                kicker.add(card);
            }
        }

        // Build final hand: four-of-a-kind first, then kicker
        List<Card> handCards = new ArrayList<>(fourKind);
        handCards.addAll(kicker);

        return Optional.of(new PokerHand(HandRank.FOUR_OF_A_KIND, handCards));
    }

    private Map<Rank, List<Card>> groupByRank(List<Card> cards) {
        Map<Rank, List<Card>> groups = new HashMap<>();
        for (Card card : cards) {
            groups.computeIfAbsent(card.getRank(), k -> new ArrayList<>()).add(card);
        }
        return groups;
    }
}

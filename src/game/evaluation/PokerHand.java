package game.evaluation;

import cards.Card;
import cards.Rank;
import java.util.*;

/**
 * Immutable class representing a poker hand with its rank and cards.
 * Implements Comparable for proper hand comparison including tiebreakers.
 */
public class PokerHand implements Comparable<PokerHand> {
    private final HandRank rank;
    private final List<Card> cards;
    private final List<Rank> ranksForComparison;

    public PokerHand(HandRank rank, List<Card> cards) {
        this.rank = rank;
        this.cards = List.copyOf(cards); // Immutable copy
        this.ranksForComparison = extractRanksForComparison(cards);
    }

    /**
     * Extracts ranks in order of importance for tiebreaking.
     * For example, in a pair, the pair rank comes first, then kickers.
     */
    private List<Rank> extractRanksForComparison(List<Card> cards) {
        List<Rank> ranks = new ArrayList<>();
        for (Card card : cards) {
            ranks.add(card.getRank());
        }
        return List.copyOf(ranks);
    }

    public HandRank getRank() {
        return rank;
    }

    public List<Card> getCards() {
        return cards;
    }

    @Override
    public int compareTo(PokerHand other) {
        // First compare by hand rank
        int rankComparison = Integer.compare(this.rank.getValue(), other.rank.getValue());
        if (rankComparison != 0) {
            return rankComparison;
        }

        // If same rank, compare card by card (kickers)
        for (int i = 0; i < Math.min(ranksForComparison.size(), other.ranksForComparison.size()); i++) {
            int cardComparison = Integer.compare(
                getRankValue(ranksForComparison.get(i)),
                getRankValue(other.ranksForComparison.get(i))
            );
            if (cardComparison != 0) {
                return cardComparison;
            }
        }

        return 0; // Hands are equal
    }

    /**
     * Gets numeric value for rank comparison (ACE is highest).
     */
    private int getRankValue(Rank rank) {
        return rank.ordinal();
    }

    @Override
    public String toString() {
        return rank.getDisplayName() + ": " + cards;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PokerHand)) return false;
        PokerHand that = (PokerHand) o;
        return rank == that.rank && cards.equals(that.cards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rank, cards);
    }
}

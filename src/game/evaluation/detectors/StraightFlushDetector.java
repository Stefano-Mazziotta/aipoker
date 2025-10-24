package game.evaluation.detectors;

import cards.Card;
import cards.Suit;
import game.evaluation.HandDetector;
import game.evaluation.HandRank;
import game.evaluation.PokerHand;
import java.util.*;

/**
 * Detects Straight Flush hands (five consecutive ranks of the same suit).
 * This is the highest-ranking hand in poker.
 */
public class StraightFlushDetector implements HandDetector {
    
    private final StraightDetector straightDetector = new StraightDetector();
    private final FlushDetector flushDetector = new FlushDetector();

    @Override
    public Optional<PokerHand> detect(List<Card> cards) {
        if (cards.size() != 5) {
            return Optional.empty();
        }

        // Must be both a flush and a straight
        Optional<PokerHand> flush = flushDetector.detect(cards);
        Optional<PokerHand> straight = straightDetector.detect(cards);

        if (flush.isPresent() && straight.isPresent()) {
            // Use the same card order as the straight
            return Optional.of(new PokerHand(HandRank.STRAIGHT_FLUSH, straight.get().getCards()));
        }

        return Optional.empty();
    }
}

package game.evaluation;

import cards.Card;
import java.util.List;
import java.util.Optional;

/**
 * Interface for detecting specific poker hand types.
 * Each implementation is responsible for detecting exactly one hand type.
 */
public interface HandDetector {
    /**
     * Attempts to detect a specific poker hand from exactly 5 cards.
     * 
     * @param cards Exactly 5 cards to check
     * @return Optional containing the detected hand, or empty if not detected
     */
    Optional<PokerHand> detect(List<Card> cards);
}

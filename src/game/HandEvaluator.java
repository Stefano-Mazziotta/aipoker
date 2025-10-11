package game;

import cards.Card;
import java.util.*;

public class HandEvaluator {
    public static int evaluateBestHand(List<Card> holeCards, List<Card> communityCards) {
        List<Card> all = new ArrayList<>(holeCards);
        all.addAll(communityCards);

        // TODO: lógica real de ranking
        return new Random().nextInt(1000);
    }
}

package game;

import cards.Card;
import java.util.*;

public class HandEvaluator 
{
    public static int evaluateBestHand(List<Card> holeCards, List<Card> communityCards) 
    {
        // combine cards
        List<Card> allCards = new ArrayList<>(holeCards);
        allCards.addAll(communityCards);

        // TODO: implement ranking logic
        return 0; // return hand strength as integer
    }
}
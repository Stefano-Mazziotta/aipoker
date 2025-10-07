package game;

import cards.Card;
import java.util.List;
import java.util.ArrayList;

public class Player {
    private final String name;
    private final List<Card> holeCards = new ArrayList<>();
    private int chips;

    public Player(String name, int startingChips) {
        this.name = name;
        this.chips = startingChips;
    }

    public void addCard(Card card) {
        holeCards.add(card);
    }

    public List<Card> getHoleCards() {
        return holeCards;
    }
    
    public Boolean hasMinimumChips(Integer min) {
    	return this.chips >= min;
    }

    public String getName() { return name; }
    public int getChips() { return chips; }
    public void bet(int amount) { chips -= amount; }
    public void win(int amount) { chips += amount; }
}
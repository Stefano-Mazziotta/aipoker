package game;

import cards.Card;
import java.util.*;

public class Player {
    private final String name;
    private int cash;
    private List<Card> holeCards = new ArrayList<>();
    private boolean folded = false;

    public Player(String name, int cash) {
        this.name = name;
        this.cash = cash;
    }

    public String getName() { return name; }
    
    public int getCash() { return cash; }
    public void setCash(int cash) { this.cash = cash; }

    public List<Card> getHoleCards() { return holeCards; }
    public void clearHoleCards() { holeCards.clear(); }
    public void addCard(Card card) { holeCards.add(card); }
    
    public boolean isFolded() { return folded; }
    public void setFolded(boolean folded) { this.folded = folded; }
    
    @Override
    public String toString() {
        return name + " (" + cash + " cash)";
    }
}
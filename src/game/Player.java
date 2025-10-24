package game;

import cards.Card;
import java.util.*;

public class Player {
    private final String name;
    private int cash;
    private List<Card> holeCards;
    private boolean folded;

    public Player(String name, int cash) {
        this.name = name;
        this.cash = cash;
        this.holeCards = new ArrayList<>();
        this.folded = false;
    }

    public void addCard(Card card) {
        holeCards.add(card);
    }

    public void clearHoleCards() {
        holeCards.clear();
    }

    public void addCash(int amount) {
        this.cash += amount;
    }

    public void subtractCash(int amount) {
        this.cash -= amount;
    }

    public String getName() {
        return name;
    }

    public int getCash() {
        return cash;
    }

    public List<Card> getHoleCards() {
        return holeCards;
    }

    public boolean isFolded() {
        return folded;
    }

    public void setFolded(boolean folded) {
        this.folded = folded;
    }
    
    public boolean isBroke(int bindAmount) {
    	return cash < bindAmount;
    }
    
    @Override
    public String toString() {
        return name + " ($" + cash + ")";
    }
}
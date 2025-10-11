package game;

import cards.*;
import java.util.*;

public class Table {
    private Deck deck;
    private final HashSet<Player> players;
    private List<Card> communityCards;
    private Player winner;
    private List<Round> rounds;
    private Round currentRound;

    public Table(HashSet<Player> players) {
        this.players = players;
        this.deck = new Deck();
        this.communityCards = new ArrayList<>();
        this.rounds = new ArrayList<>();
    }

    public void startNewRound() {
        resetRound();
        this.currentRound = new Round(new ArrayList<>(players));
        this.rounds.add(currentRound);
    }

    public Round getCurrentRound() {
        return currentRound;
    }

    public void dealHoleCards() {
        for (int i = 0; i < 2; i++) {
            for (Player player : players) {
                player.addCard(deck.dealCard());
            }
        }
    }

    public void dealFlop() {
        deck.dealCard(); // burn card
        for (int i = 0; i < 3; i++) {
            communityCards.add(deck.dealCard());
        }
    }

    public void dealTurn() {
        deck.dealCard(); // burn card
        communityCards.add(deck.dealCard());
    }

    public void dealRiver() {
        deck.dealCard(); // burn card
        communityCards.add(deck.dealCard());
    }

    public void determineWinner() {
        Player best = null;
        int bestScore = -1;
        for (Player player : players) {
            if (player.isFolded())
                continue;
            
            int score = HandEvaluator.evaluateBestHand(player.getHoleCards(), communityCards);
            if (score > bestScore) {
                bestScore = score;
                best = player;
            }
        }
        this.winner = best;
        System.out.println("Winner: " + (best != null ? best.getName() : "None"));
    }

    public void resetRound() {
        this.deck = new Deck();
        this.deck.shuffle();
        this.communityCards.clear();
        for (Player player : players) {
            player.clearHoleCards();
            player.setFolded(false);
        }
    }

    public void setWinner(Player winner) { 
        this.winner = winner; 
    }

    public Player getWinner() { 
        return winner; 
    }

    public boolean existWinner() { 
        return winner != null; 
    }

    public List<Card> getCommunityCards() { 
        return communityCards; 
    }

    public Deck getDeck() { 
        return deck; 
    }

    public Set<Player> getPlayers() { 
        return players; 
    }

    public List<Round> getRounds() {
        return rounds;
    }

    public void removeBrokePlayers() {
        players.removeIf(player -> player.getCash() <= 0);
        if (players.size() == 1)
            winner = players.iterator().next();
    }
}
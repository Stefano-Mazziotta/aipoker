package game;

import cards.*;
import java.util.*;

public class Table {
    private Deck deck;
    private final HashSet<Player> players;
    private List<Card> communityCards;
    private Player winner;

    public Table(HashSet<Player> players) {
        this.players = players;
        this.deck = new Deck();
        this.communityCards = new ArrayList<>();
    }

    public void resetRound() {
        this.deck = new Deck();
        this.communityCards.clear();
        for (Player player : players) {
            player.clearHoleCards();
            player.setFolded(false);
        }
    }

    public void setWinner(Player winner) { this.winner = winner; }
    public Player getWinner() { return winner; }
    public boolean existWinner() { return winner != null; }
    public List<Card> getCommunityCards() { return communityCards; }
    public Deck getDeck() { return deck; }
    public Set<Player> getPlayers() { return players; }

    public void removeBrokePlayers() {
        players.removeIf(player -> player.getCash() <= 0);
        if (players.size() == 1)
            winner = players.iterator().next();
    }
}
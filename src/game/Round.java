package game;

import java.util.*;

public class Round {
    private final List<Player> players;
    private int pot;
    private List<Player> activePlayers;

    public Round(List<Player> players) {
        this.players = new ArrayList<>(players);
        this.activePlayers = new ArrayList<>(players);
        this.pot = 0;
    }

    public void addToPot(int amount) {
        this.pot += amount;
    }

    public int getPot() {
        return pot;
    }

    public List<Player> getActivePlayers() {
        return activePlayers;
    }

    public void removePlayer(Player player) {
        activePlayers.remove(player);
    }

    public boolean hasMultipleActivePlayers() {
        return activePlayers.size() > 1;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void distributePot(Player winner) {
        winner.addCash(pot);
        pot = 0;
    }
}
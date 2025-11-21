package com.poker.game.domain.model;

import com.poker.player.domain.model.Player;
import java.util.*;

/**
 * Entity representing a single betting round in a poker game.
 * Manages the pot and tracks active players.
 * 
 * REFACTORED from existing Round.java with enhancements.
 */
public class Round {
    private final List<Player> players;
    private Pot pot;
    private final List<Player> activePlayers;
    private int currentBet;
    private final Map<String, Integer> playerBets; // Track each player's contribution this round

    public Round(List<Player> players) {
        this.players = new ArrayList<>(players);
        this.activePlayers = new ArrayList<>(players);
        this.pot = Pot.empty();
        this.currentBet = 0;
        this.playerBets = new HashMap<>();
    }

    public void addToPot(int amount) {
        this.pot = this.pot.add(amount);
    }
    
    public void recordPlayerBet(Player player, int amount) {
        String playerId = player.getId().getValue().toString();
        playerBets.put(playerId, playerBets.getOrDefault(playerId, 0) + amount);
    }
    
    public void setPlayerBet(Player player, int amount) {
        String playerId = player.getId().getValue().toString();
        playerBets.put(playerId, amount);
    }
    
    public int getPlayerBet(Player player) {
        String playerId = player.getId().getValue().toString();
        return playerBets.getOrDefault(playerId, 0);
    }

    public void setCurrentBet(int amount) {
        this.currentBet = amount;
    }

    public int getCurrentBet() {
        return currentBet;
    }

    public Pot getPot() {
        return pot;
    }

    public List<Player> getActivePlayers() {
        return activePlayers.stream()
            .filter(p -> !p.isFolded())
            .toList();
    }

    public List<Player> getAllPlayers() {
        return new ArrayList<>(players);
    }

    public void removePlayer(Player player) {
        activePlayers.remove(player);
    }

    public boolean hasMultipleActivePlayers() {
        return getActivePlayers().size() > 1;
    }

    public void distributePot(Player winner) {
        winner.addChips(pot.getAmount());
        this.pot = Pot.empty();
    }

    public void reset() {
        this.pot = Pot.empty();
        this.currentBet = 0;
        this.activePlayers.clear();
        this.activePlayers.addAll(players);
        this.playerBets.clear();
    }
    
    public Map<String, Integer> getAllPlayerBets() {
        return new HashMap<>(playerBets);
    }
}

package game;

import java.util.*;

public class PokerGame {
    private final Table table;

    public PokerGame(HashSet<Player> players) {
        PokerGameValidator.validatePlayers(players);
        this.table = new Table(players);
    }

    public void start() {
        System.out.println("=== Poker Game Started ===");
        
        while (table.getWinnerGamePlayer() == null) {
            table.play();
        }
        
        System.out.println("\n=== Game Over ===");
        System.out.println("Winner: " + table.getWinnerGamePlayer());
    }
}
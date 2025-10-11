package game;

import java.util.*;

public class BettingRound {
    private final Table table;
    private final Round round;
    private final String name;

    public BettingRound(Table table, Round round, String name) {
        this.table = table;
        this.round = round;
        this.name = name;
    }

    public void play() {
        System.out.println("\n=== " + name + " ===");
        
        if (!round.hasMultipleActivePlayers()) {
            System.out.println("Only one player remaining, skipping betting round");
            return;
        }

        for (Player player : round.getActivePlayers()) {
            if (player.isFolded()) continue;

            System.out.println(player.getName() + "'s turn (Cash: $" + player.getCash() + ")");
            
            // Simple betting logic - all players check
            System.out.println(player.getName() + " checks");
        }
    }
}
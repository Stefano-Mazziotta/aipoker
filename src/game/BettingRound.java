package game;

import java.util.*;

public class BettingRound {
    private final Round round;
    private final String name;

    public BettingRound(Round round, String name) {
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
            player.subtractCash(50);
            this.round.addToPot(50);
            // Simple betting logic - all players check
            //System.out.println(player.getName() + " checks");
        }
    }
}
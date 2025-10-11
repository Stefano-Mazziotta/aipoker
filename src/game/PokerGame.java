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
        
        while (!table.existWinner()) {
            playRound();
            table.removeBrokePlayers();
        }
        
        System.out.println("\n=== Game Over ===");
        System.out.println("Winner: " + table.getWinner().getName());
    }

    private void playRound() {
        System.out.println("\n--- Starting New Round ---");
        
        // Start new round
        table.startNewRound();
        Round currentRound = table.getCurrentRound();
        
        // Deal hole cards
        table.dealHoleCards();
        
        // Pre-flop betting
        BettingRound preFlop = new BettingRound(table, currentRound, "Pre-Flop");
        preFlop.play();
        
        if (!currentRound.hasMultipleActivePlayers()) {
            awardPotToLastPlayer(currentRound);
            return;
        }
        
        // Flop
        table.dealFlop();
        System.out.println("Community cards: " + table.getCommunityCards());
        BettingRound flopBetting = new BettingRound(table, currentRound, "Flop");
        flopBetting.play();
        
        if (!currentRound.hasMultipleActivePlayers()) {
            awardPotToLastPlayer(currentRound);
            return;
        }
        
        // Turn
        table.dealTurn();
        System.out.println("Community cards: " + table.getCommunityCards());
        BettingRound turnBetting = new BettingRound(table, currentRound, "Turn");
        turnBetting.play();
        
        if (!currentRound.hasMultipleActivePlayers()) {
            awardPotToLastPlayer(currentRound);
            return;
        }
        
        // River
        table.dealRiver();
        System.out.println("Community cards: " + table.getCommunityCards());
        BettingRound riverBetting = new BettingRound(table, currentRound, "River");
        riverBetting.play();
        
        // Showdown
        table.determineWinner();
        currentRound.distributePot(table.getWinner());
    }

    private void awardPotToLastPlayer(Round round) {
        Player lastPlayer = round.getActivePlayers().get(0);
        System.out.println("All other players folded. " + lastPlayer.getName() + " wins the pot!");
        round.distributePot(lastPlayer);
    }

    public Table getTable() {
        return table;
    }
}
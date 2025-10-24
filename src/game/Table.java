package game;

import cards.*;
import java.util.*;

public class Table {
	private static int MIN_BIND_VALUE = 50;
	
    private Deck deck;
    private final HashSet<Player> players;
    private List<Player> activePlayers;
    private List<Card> communityCards;
    private Player handWinner;
    private List<Round> rounds;
    private Round currentRound;

    public Table(HashSet<Player> players) {
        this.players = players;
        this.activePlayers = new ArrayList<>(players);
        this.deck = new Deck();
        this.communityCards = new ArrayList<>();
        this.rounds = new ArrayList<>();
    }
    
    public Player getWinnerGamePlayer() {
    	if(this.activePlayers.size() == 1) {
    		 return this.activePlayers.getFirst();
    	}
    	
    	return null;
    }
    
    public void play() {
        System.out.println("\n--- Starting New Round ---");
        
        startNewHand();
        dealHoleCards();
        
        // Pre-flop betting
        BettingRound preFlop = new BettingRound(currentRound, "Pre-Flop");
        preFlop.play();
        
        if (!currentRound.hasMultipleActivePlayers()) {
            awardPotToLastPlayer(currentRound);
            return;
        }
        
        // Flop
        dealFlop();
        System.out.println("Community cards: " + getCommunityCards());
        BettingRound flopBetting = new BettingRound(currentRound, "Flop");
        flopBetting.play();
        
        if (!currentRound.hasMultipleActivePlayers()) {
            awardPotToLastPlayer(currentRound);
            return;
        }
        
        // Turn
        dealTurn();
        System.out.println("Community cards: " + getCommunityCards());
        BettingRound turnBetting = new BettingRound(currentRound, "Turn");
        turnBetting.play();
        
        if (!currentRound.hasMultipleActivePlayers()) {
            awardPotToLastPlayer(currentRound);
            return;
        }
        
        // River
        dealRiver();
        System.out.println("Community cards: " + getCommunityCards());
        BettingRound riverBetting = new BettingRound(currentRound, "River");
        riverBetting.play();
        
        determineHandWinner();
        currentRound.distributePot(this.handWinner);
        removeBrokePlayers();
    }

    private void startNewHand() {
        resetRound();
        this.currentRound = new Round(new ArrayList<>(players));
        this.rounds.add(currentRound);
    }

    private void dealHoleCards() {
        for (int i = 0; i < 2; i++) {
            for (Player player : activePlayers) {
                player.addCard(deck.dealCard());
            }
        }
    }

    private void dealFlop() {
        deck.dealCard(); // burn card
        for (int i = 0; i < 3; i++) {
            communityCards.add(deck.dealCard());
        }
    }

    private void dealTurn() {
        deck.dealCard(); // burn card
        communityCards.add(deck.dealCard());
    }

    private void dealRiver() {
        deck.dealCard(); // burn card
        communityCards.add(deck.dealCard());
    }

    private void determineHandWinner() {
        Player best = null;
        int bestScore = -1;
        for (Player player : activePlayers) {
            if (player.isFolded())
                continue;
            
            int score = HandEvaluator.evaluateBestHand(player.getHoleCards(), communityCards);
            if (score > bestScore) {
                bestScore = score;
                best = player;
            }
        }
        this.handWinner = best;
        System.out.println("Winner: " + (best != null ? best.getName() : "None"));
    }

    private void resetRound() {
        this.deck = new Deck();
        this.deck.shuffle();
        this.communityCards.clear();
        for (Player player : activePlayers) {
            player.clearHoleCards();
            player.setFolded(false);
        }
    }

    private List<Card> getCommunityCards() { 
        return communityCards; 
    }

    private void awardPotToLastPlayer(Round round) {
        Player lastPlayer = round.getActivePlayers().get(0);
        System.out.println("All other players folded. " + lastPlayer.getName() + " wins the pot!");
        round.distributePot(lastPlayer);
    }
    
    private void removeBrokePlayers(){
    	activePlayers.removeIf(player -> player.isBroke(MIN_BIND_VALUE));
    }
}
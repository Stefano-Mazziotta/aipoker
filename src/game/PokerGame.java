package game;

import cards.Card;
import cards.Deck;
import game.validation.PokerGameValidator;

import java.util.*;

public class PokerGame {
	private final int MINIMUM_CHIPS = 50;
    private Deck deck;
    private final HashSet<Player> players;
    private List<Card> communityCards;
    private Player winner;

    public PokerGame(HashSet<Player> players) {
        PokerGameValidator.validatePlayers(players);
        this.players = players;
        this.deck = new Deck();
        PokerGameValidator.validateDeck(deck);
        this.communityCards = new ArrayList<>();
    }

    public void start() {
    	
    	// the game run this round behavior in loop
    	// game end when exist only one player with chips
    	// if a player get 0 chips, must be kicked from game
    	
    	while(!this.existWinner()) {
    		// complete round
        	
        	// 1. shuffle
        	// 2. deal hole cards
        	// 3. deal community cards
        	// 4. bet round
        	// 5. show flop
        	// 6. bet round
        	// 7. show turn
        	// 8. bet round
        	// 9. show river
        	// 10. bet round
        	// 11. choose winner of round (best hand ranking)
            resetRound();
            deck.shuffle();
            dealHoleCards();
            dealCommunityCards();
            showHands();
    	}
    }

    private void resetRound() {
        this.deck = new Deck();
        this.communityCards.clear();
        for (Player p : players) {
            p.getHoleCards().clear();
        }
    }

    private void dealHoleCards() {
        for (int i = 0; i < 2; i++) {
            for (Player p : players) {
                p.addCard(deck.dealCard());
            }
        }
    }

    private void dealCommunityCards() {
        for (int i = 0; i < 3; i++) {
            communityCards.add(deck.dealCard()); // flop
        }
        communityCards.add(deck.dealCard()); // turn
        communityCards.add(deck.dealCard()); // river
    }
    
    private Boolean existWinner(){
    	return this.winner != null;
    }

    private void showHands() {
        System.out.println("Community cards: " + communityCards);
        for (Player p : players) {
            System.out.println(p.getName() + " hole cards: " + p.getHoleCards());
        }
    }
}

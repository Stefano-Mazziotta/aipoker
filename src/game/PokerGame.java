package game;

import cards.Card;
import cards.Deck;
import game.validation.PokerGameValidator;

import java.util.*;

public class PokerGame {
    private Deck deck;
    private final List<Player> players;
    private List<Card> communityCards;

    public PokerGame(List<Player> players) {
        PokerGameValidator.validatePlayers(players);
        this.players = players;
        this.deck = new Deck();
        PokerGameValidator.validateDeck(deck);
        this.communityCards = new ArrayList<>();
    }

    public void start() {
        resetRound();
        deck.shuffle();
        dealHoleCards();
        dealCommunityCards();
        showHands();
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

    private void showHands() {
        System.out.println("Community cards: " + communityCards);
        for (Player p : players) {
            System.out.println(p.getName() + " hole cards: " + p.getHoleCards());
        }
    }
}

package com.poker.game.domain.model;

import com.poker.player.domain.model.Player;
import com.poker.shared.domain.valueobject.*;
import com.poker.game.domain.evaluation.*;
import com.poker.game.domain.exception.*;
import java.util.*;

/**
 * Game Aggregate Root.
 * Manages the complete lifecycle of a Texas Hold'em poker game.
 * 
 * MERGED from PokerGame + Table with proper state management.
 */
public class Game {
    private final GameId id;
    private GameState state;
    private final Blinds blinds;
    private Deck deck;
    private final List<Player> players;
    private final List<Card> communityCards;
    private Round currentRound;
    private final HandEvaluationStrategy evaluator;
    private int dealerPosition;

    private Game(GameId id, List<Player> players, Blinds blinds) {
        validatePlayers(players);
        this.id = id;
        this.players = new ArrayList<>(players);
        this.blinds = blinds;
        this.state = GameState.WAITING;
        this.deck = new Deck();
        this.communityCards = new ArrayList<>();
        this.evaluator = new TexasHoldemEvaluator();
        this.dealerPosition = 0;
    }

    private void validatePlayers(List<Player> players) {
        if (players.size() < 2 || players.size() > 9) {
            throw new IllegalArgumentException(
                "Game requires 2-9 players, got: " + players.size()
            );
        }
    }

    public static Game create(List<Player> players, Blinds blinds) {
        return new Game(GameId.generate(), players, blinds);
    }

    public static Game reconstitute(GameId id, List<Player> players, Blinds blinds, 
                                    GameState state, int dealerPosition) {
        Game game = new Game(id, players, blinds);
        game.state = state;
        game.dealerPosition = dealerPosition;
        return game;
    }

    public void start() {
        if (state != GameState.WAITING) {
            throw new InvalidGameStateException("Game already started");
        }
        this.state = GameState.PRE_FLOP;
        startNewHand();
    }

    private void startNewHand() {
        resetForNewHand();
        postBlinds();
        dealHoleCards();
    }

    private void resetForNewHand() {
        this.deck = new Deck();
        this.deck.shuffle();
        this.communityCards.clear();
        this.currentRound = new Round(players);
        
        players.forEach(p -> {
            p.clearHand();
            p.resetFoldedStatus();
        });
    }

    private void postBlinds() {
        int smallBlindPos = (dealerPosition + 1) % players.size();
        int bigBlindPos = (dealerPosition + 2) % players.size();
        
        Player smallBlindPlayer = players.get(smallBlindPos);
        Player bigBlindPlayer = players.get(bigBlindPos);
        
        smallBlindPlayer.subtractChips(blinds.getSmallBlind());
        bigBlindPlayer.subtractChips(blinds.getBigBlind());
        
        currentRound.addToPot(blinds.getSmallBlind());
        currentRound.addToPot(blinds.getBigBlind());
        currentRound.setCurrentBet(blinds.getBigBlind());
    }

    private void dealHoleCards() {
        for (int i = 0; i < 2; i++) {
            for (Player player : players) {
                player.receiveCard(deck.dealCard());
            }
        }
    }

    public void dealFlop() {
        if (state != GameState.PRE_FLOP) {
            throw new InvalidGameStateException("Cannot deal flop in state: " + state);
        }
        deck.dealCard(); // Burn card
        communityCards.add(deck.dealCard());
        communityCards.add(deck.dealCard());
        communityCards.add(deck.dealCard());
        this.state = GameState.FLOP;
    }

    public void dealTurn() {
        if (state != GameState.FLOP) {
            throw new InvalidGameStateException("Cannot deal turn in state: " + state);
        }
        deck.dealCard(); // Burn card
        communityCards.add(deck.dealCard());
        this.state = GameState.TURN;
    }

    public void dealRiver() {
        if (state != GameState.TURN) {
            throw new InvalidGameStateException("Cannot deal river in state: " + state);
        }
        deck.dealCard(); // Burn card
        communityCards.add(deck.dealCard());
        this.state = GameState.RIVER;
    }

    public Player determineWinner() {
        this.state = GameState.SHOWDOWN;
        
        Player bestPlayer = null;
        PokerHand bestHand = null;

        for (Player player : players) {
            if (player.isFolded()) continue;

            List<Card> allCards = new ArrayList<>(player.getHand().getCards());
            allCards.addAll(communityCards);

            PokerHand hand = evaluator.evaluate(allCards);
            
            if (bestHand == null || hand.compareTo(bestHand) > 0) {
                bestHand = hand;
                bestPlayer = player;
            }
        }

        if (bestPlayer != null) {
            currentRound.distributePot(bestPlayer);
        }

        this.state = GameState.FINISHED;
        return bestPlayer;
    }

    public void advanceDealer() {
        this.dealerPosition = (dealerPosition + 1) % players.size();
    }

    // Getters
    public GameId getId() { return id; }
    public GameState getState() { return state; }
    public Blinds getBlinds() { return blinds; }
    public List<Card> getCommunityCards() { return List.copyOf(communityCards); }
    public List<Player> getPlayers() { return List.copyOf(players); }
    public Pot getCurrentPot() { return currentRound.getPot(); }
    public Round getCurrentRound() { return currentRound; }
    public int getDealerPosition() { return dealerPosition; }
}

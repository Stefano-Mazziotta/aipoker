package com.poker.game.domain.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.poker.game.domain.evaluation.HandEvaluationStrategy;
import com.poker.game.domain.evaluation.PokerHand;
import com.poker.game.domain.evaluation.TexasHoldemEvaluator;
import com.poker.game.domain.exception.InvalidGameStateException;
import com.poker.player.domain.model.Player;
import com.poker.shared.domain.valueobject.Card;
import com.poker.shared.domain.valueobject.Deck;

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
    private int currentPlayerIndex;
    private Set<String> playersActedThisRound;

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
        this.currentPlayerIndex = 0;
        this.playersActedThisRound = new HashSet<>();
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
                                    GameState state, int dealerPosition, int potAmount, int currentBet,
                                    Map<String, Integer> playerBets, List<Card> communityCards) {
        Game game = new Game(id, players, blinds);
        game.state = state;
        game.dealerPosition = dealerPosition;
        
        // ALWAYS create a fresh deck when reconstituting - never restore deck state
        // This prevents duplicate cards issue
        game.deck = new Deck();
        game.deck.shuffle();
        
        // Restore community cards
        if (communityCards != null && !communityCards.isEmpty()) {
            game.communityCards.addAll(communityCards);
        }
        
        // If game is in progress, initialize the current round with saved state
        if (state != GameState.WAITING && state != GameState.FINISHED) {
            game.currentRound = new Round(players);
            // Restore pot and current bet
            if (potAmount > 0) {
                game.currentRound.addToPot(potAmount);
            }
            game.currentRound.setCurrentBet(currentBet);
            
            // Restore player bets
            if (playerBets != null && !playerBets.isEmpty()) {
                for (Player player : players) {
                    String playerId = player.getId().getValue().toString();
                    int playerBet = playerBets.getOrDefault(playerId, 0);
                    if (playerBet > 0) {
                        // Don't use recordPlayerBet as that adds to existing - just set directly
                        game.currentRound.setPlayerBet(player, playerBet);
                    }
                }
            }
            
            // Initialize turn tracking for reconstituted game
            game.currentPlayerIndex = (dealerPosition + 1) % players.size();
            game.playersActedThisRound = new HashSet<>();
        }
        
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
        
        // Record blind bets so players don't have to re-pay them
        currentRound.setPlayerBet(smallBlindPlayer, blinds.getSmallBlind());
        currentRound.setPlayerBet(bigBlindPlayer, blinds.getBigBlind());
    }

    private void dealHoleCards() {
        for (int i = 0; i < 2; i++) {
            for (Player player : players) {
                player.receiveCard(deck.dealCard());
            }
        }
        // Initialize first player to act (left of big blind in pre-flop)
        this.currentPlayerIndex = (dealerPosition + 3) % players.size();
        this.playersActedThisRound = new HashSet<>();
    }

    public void dealFlop() {
        if (state != GameState.PRE_FLOP) {
            throw new InvalidGameStateException("Cannot deal flop in state: " + state);
        }
        if (!isBettingRoundComplete()) {
            throw new InvalidGameStateException("Cannot deal flop: Pre-flop betting round not complete");
        }
        deck.dealCard(); // Burn card
        communityCards.add(deck.dealCard());
        communityCards.add(deck.dealCard());
        communityCards.add(deck.dealCard());
        this.state = GameState.FLOP;
        startNewBettingRound();
    }

    public void dealTurn() {
        if (state != GameState.FLOP) {
            throw new InvalidGameStateException("Cannot deal turn in state: " + state);
        }
        if (!isBettingRoundComplete()) {
            throw new InvalidGameStateException("Cannot deal turn: Flop betting round not complete");
        }
        deck.dealCard(); // Burn card
        communityCards.add(deck.dealCard());
        this.state = GameState.TURN;
        startNewBettingRound();
    }

    public void dealRiver() {
        if (state != GameState.TURN) {
            throw new InvalidGameStateException("Cannot deal river in state: " + state);
        }
        if (!isBettingRoundComplete()) {
            throw new InvalidGameStateException("Cannot deal river: Turn betting round not complete");
        }
        deck.dealCard(); // Burn card
        communityCards.add(deck.dealCard());
        this.state = GameState.RIVER;
        startNewBettingRound();
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
    
    /**
     * Get the current player who should act
     */
    public Player getCurrentPlayer() {
        if (players.isEmpty()) return null;
        return players.get(currentPlayerIndex);
    }
    
    /**
     * Check if the current player can act
     */
    public boolean isPlayerTurn(Player player) {
        if (players.isEmpty()) return false;
        Player currentPlayer = players.get(currentPlayerIndex);
        return currentPlayer.getId().equals(player.getId()) && !player.isFolded();
    }
    
    /**
     * Record that a player has acted and advance to next player
     */
    public void recordPlayerAction(Player player) {
        playersActedThisRound.add(player.getId().getValue().toString());
        advanceTurn();
    }
    
    /**
     * Advance to next active (non-folded, non-all-in) player who can act
     */
    private void advanceTurn() {
        int attempts = 0;
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            attempts++;
            // Avoid infinite loop if all players folded or all-in
            if (attempts >= players.size()) break;
            
            Player currentPlayer = players.get(currentPlayerIndex);
            // Player can act if not folded AND has chips remaining
            if (!currentPlayer.isFolded() && currentPlayer.getChipsAmount() > 0) {
                break;
            }
        } while (true);
    }
    
    /**
     * Check if betting round is complete
     */
    private boolean isBettingRoundComplete() {
        List<Player> activePlayers = currentRound.getActivePlayers();
        
        // If only one player remains, round is complete
        if (activePlayers.size() <= 1) {
            return true;
        }
        
        // All active players must have acted
        for (Player player : activePlayers) {
            String playerId = player.getId().getValue().toString();
            if (!playersActedThisRound.contains(playerId)) {
                return false;
            }
        }
        
        // All active players must have equal bets (or be all-in)
        int currentBet = currentRound.getCurrentBet();
        for (Player player : activePlayers) {
            int playerBet = currentRound.getPlayerBet(player);
            // Player must match current bet OR be all-in (0 chips)
            if (playerBet < currentBet && player.getChipsAmount() > 0) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Start a new betting round (reset action tracking)
     */
    private void startNewBettingRound() {
        playersActedThisRound.clear();
        currentRound.setCurrentBet(0);
        // First to act after flop/turn/river is left of dealer
        currentPlayerIndex = (dealerPosition + 1) % players.size();
        // Skip folded and all-in players
        int attempts = 0;
        while (attempts < players.size()) {
            Player currentPlayer = players.get(currentPlayerIndex);
            if (!currentPlayer.isFolded() && currentPlayer.getChipsAmount() > 0) {
                break;
            }
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            attempts++;
        }
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
    public int getCurrentPlayerIndex() { return currentPlayerIndex; }
    public Set<String> getPlayersActedThisRound() { return Set.copyOf(playersActedThisRound); }
    
    // Setters for persistence layer
    public void setCurrentPlayerIndex(int index) { this.currentPlayerIndex = index; }
    public void setPlayersActedThisRound(Set<String> players) { 
        this.playersActedThisRound.clear();
        this.playersActedThisRound.addAll(players);
    }
}

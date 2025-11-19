package com.poker.game.domain.model;

import com.poker.player.domain.model.Player;
import com.poker.player.domain.model.PlayerAction;
import com.poker.game.domain.exception.IllegalActionException;

/**
 * Entity managing betting logic for a game phase.
 * Handles player actions (FOLD, CHECK, CALL, RAISE, ALL_IN).
 * 
 * ENHANCED from existing with proper betting rules.
 */
public class BettingRound {
    private final Round round;
    private final GameState phase;
    private int currentBet;

    public BettingRound(Round round, GameState phase) {
        this.round = round;
        this.phase = phase;
        this.currentBet = round.getCurrentBet();
    }

    public void executePlayerAction(Player player, PlayerAction action, int amount) {
        validateAction(player, action, amount);

        switch (action) {
            case FOLD:
                player.fold();
                break;
                
            case CHECK:
                if (currentBet > 0) {
                    throw new IllegalActionException("Cannot CHECK when there's a bet to call");
                }
                break;
                
            case CALL:
                int callAmount = currentBet;
                if (!player.canAfford(callAmount)) {
                    throw new IllegalActionException("Insufficient chips to call");
                }
                player.subtractChips(callAmount);
                round.addToPot(callAmount);
                break;
                
            case RAISE:
                if (amount <= currentBet) {
                    throw new IllegalActionException("Raise amount must be greater than current bet");
                }
                if (!player.canAfford(amount)) {
                    throw new IllegalActionException("Insufficient chips to raise");
                }
                player.subtractChips(amount);
                round.addToPot(amount);
                currentBet = amount;
                round.setCurrentBet(amount);
                break;
                
            case ALL_IN:
                int allInAmount = player.getChipsAmount();
                player.subtractChips(allInAmount);
                round.addToPot(allInAmount);
                if (allInAmount > currentBet) {
                    currentBet = allInAmount;
                    round.setCurrentBet(allInAmount);
                }
                break;
        }
    }

    private void validateAction(Player player, PlayerAction action, int amount) {
        if (player.isFolded()) {
            throw new IllegalActionException("Folded player cannot act");
        }
        
        if (amount < 0) {
            throw new IllegalActionException("Amount cannot be negative");
        }
    }

    public GameState getPhase() {
        return phase;
    }

    public int getCurrentBet() {
        return currentBet;
    }

    public boolean isComplete() {
        return !round.hasMultipleActivePlayers();
    }
}

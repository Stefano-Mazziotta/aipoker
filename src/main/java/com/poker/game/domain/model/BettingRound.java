package com.poker.game.domain.model;

import com.poker.game.domain.exception.IllegalActionException;
import com.poker.player.domain.model.Player;
import com.poker.player.domain.model.PlayerAction;

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
            case FOLD -> player.fold();
                
            case CHECK -> {
                if (currentBet > 0) {
                    throw new IllegalActionException("Cannot CHECK when there's a bet to call");
                }
            }
                
            case CALL -> {
                // Calculate how much the player actually needs to call
                int playerCurrentBet = round.getPlayerBet(player);
                int amountToCall = currentBet - playerCurrentBet;
                
                if (!player.canAfford(amountToCall)) {
                    throw new IllegalActionException("Insufficient chips to call");
                }
                player.subtractChips(amountToCall);
                round.addToPot(amountToCall);
                round.recordPlayerBet(player, amountToCall);
            }
                
            case RAISE -> {
                int playerBet = round.getPlayerBet(player);
                int totalRaiseAmount = amount - playerBet;
                
                if (amount <= currentBet) {
                    throw new IllegalActionException("Raise amount must be greater than current bet");
                }
                if (!player.canAfford(totalRaiseAmount)) {
                    throw new IllegalActionException("Insufficient chips to raise");
                }
                player.subtractChips(totalRaiseAmount);
                round.addToPot(totalRaiseAmount);
                round.recordPlayerBet(player, totalRaiseAmount);
                currentBet = amount;
                round.setCurrentBet(amount);
            }
                
            case ALL_IN -> {
                int allInAmount = player.getChipsAmount();
                player.subtractChips(allInAmount);
                round.addToPot(allInAmount);
                round.recordPlayerBet(player, allInAmount);
                
                int playerTotalBet = round.getPlayerBet(player);
                if (playerTotalBet > currentBet) {
                    currentBet = playerTotalBet;
                    round.setCurrentBet(playerTotalBet);
                }
            }
        }
    }

    private void validateAction(Player player, PlayerAction action, int amount) {
        if (player.isFolded()) {
            throw new IllegalActionException("Folded player cannot act");
        }
        
        // Validate action-specific constraints
        if (action == PlayerAction.RAISE && amount < 0) {
            throw new IllegalActionException("Raise amount cannot be negative");
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

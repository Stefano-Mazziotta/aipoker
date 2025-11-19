package com.poker.player.domain.model;

import com.poker.shared.domain.valueobject.Card;
import com.poker.shared.domain.exception.ValidationException;
import java.util.Objects;

/**
 * Player aggregate root in DDD terms.
 * Represents a poker player with identity, chips, and current hand state.
 * 
 * REFACTORED from existing Player class with proper value objects.
 */
public class Player {
    private final PlayerId id;
    private final String name;
    private Chips chips;
    private PlayerHand hand;
    private boolean folded;

    public Player(PlayerId id, String name, Chips initialChips) {
        this.id = Objects.requireNonNull(id, "Player ID cannot be null");
        this.name = validateName(name);
        this.chips = Objects.requireNonNull(initialChips, "Initial chips cannot be null");
        this.hand = PlayerHand.empty();
        this.folded = false;
    }

    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Player name cannot be empty");
        }
        if (name.length() > 50) {
            throw new ValidationException("Player name too long (max 50 characters)");
        }
        return name.trim();
    }

    // Factory methods
    public static Player create(String name, int initialChips) {
        return new Player(PlayerId.generate(), name, Chips.of(initialChips));
    }

    public static Player reconstitute(PlayerId id, String name, int chips, boolean folded) {
        Player player = new Player(id, name, Chips.of(chips));
        player.folded = folded;
        return player;
    }

    // Domain methods
    public void receiveCard(Card card) {
        this.hand = this.hand.addCard(card);
    }

    public void clearHand() {
        this.hand = PlayerHand.empty();
    }

    public void addChips(int amount) {
        this.chips = this.chips.add(amount);
    }

    public void subtractChips(int amount) {
        this.chips = this.chips.subtract(amount);
    }

    public void fold() {
        this.folded = true;
    }

    public void resetFoldedStatus() {
        this.folded = false;
    }

    public boolean canAfford(int amount) {
        return chips.canAfford(amount);
    }

    public boolean isBroke(int minimumRequired) {
        return chips.getAmount() < minimumRequired;
    }

    // Getters
    public PlayerId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Chips getChips() {
        return chips;
    }

    public int getChipsAmount() {
        return chips.getAmount();
    }

    public PlayerHand getHand() {
        return hand;
    }

    public boolean isFolded() {
        return folded;
    }

    @Override
    public String toString() {
        return name + " (" + chips + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player)) return false;
        Player player = (Player) o;
        return id.equals(player.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

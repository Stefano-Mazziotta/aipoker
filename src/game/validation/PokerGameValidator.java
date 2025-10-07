package game.validation;

import game.Player;
import cards.Deck;

import java.util.HashSet;
import java.util.Set;

public class PokerGameValidator {

    public static void validatePlayers(HashSet<Player> players) {
        if (players == null || players.size() < 2) {
            throw new IllegalArgumentException("Poker game requires at least 2 players.");
        }

        Set<String> names = new HashSet<>();
        for (Player player : players) {
            if (player == null) {
                throw new IllegalArgumentException("Player cannot be null.");
            }
            if (player.getChips() <= 0) {
                throw new IllegalArgumentException("Player " + player.getName() + " must have chips to play.");
            }
            if (!names.add(player.getName())) {
                throw new IllegalArgumentException("Duplicate player name detected: " + player.getName());
            }
        }
    }

    public static void validateDeck(Deck deck) {
        if (deck == null) {
            throw new IllegalArgumentException("Deck cannot be null.");
        }
        // optional: verify 52 cards at start
    }
}
package game;

import cards.Deck;

import java.util.HashSet;
import java.util.Set;

public class PokerGameValidator {
	
	private static int MINIMUM_PLAYERS = 2;
	private static int MAXIMUM_PLAYERS = 10;

    public static void validatePlayers(HashSet<Player> players) {
    	boolean hasPlayers = players != null;
    	boolean validTotalPlayers = players.size() >= MINIMUM_PLAYERS && players.size() <= MAXIMUM_PLAYERS;
        if (!hasPlayers || !validTotalPlayers) {
            throw new IllegalArgumentException("Poker game requires at least 2 players and a maximum of 10 players.");
        }

        Set<String> names = new HashSet<>();
        for (Player player : players) {
            if (player == null) {
                throw new IllegalArgumentException("Player cannot be null.");
            }
            if (player.getCash() <= 0) {
                throw new IllegalArgumentException("Player " + player.getName() + " must have cash to play.");
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
    }
}
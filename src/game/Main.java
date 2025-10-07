package game;

import java.util.Arrays;
import java.util.HashSet;

public class Main {
	
	public static void main(String[] args) {
	    HashSet<Player> players = new HashSet<Player>();
	    players.add(new Player("Alice", 1000));
	    players.add(new Player("Bob", 1000));

	    PokerGame game = new PokerGame(players);
	    game.start();
	}
	
}

package game;

import java.util.Arrays;
import java.util.List;

public class Main {
	
	public static void main(String[] args) {
	    List<Player> players = Arrays.asList(
	        new Player("Alice", 1000),
	        new Player("Bob", 1000)
	    );

	    PokerGame game = new PokerGame(players);
	    game.start();
	}
	
}

package game;

import java.util.HashSet;

public class Main {
	
	public static void main(String[] args) {
		HashSet<Player> players = new HashSet<>();
		players.add(new Player("Steff", 100));
		players.add(new Player("Luciano", 100));
		players.add(new Player("Pablo", 100));

		PokerGame game = new PokerGame(players);
		game.start();
	}
}

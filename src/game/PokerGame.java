package game;

import java.util.HashSet;

public class PokerGame {
    private final Table table;

    public PokerGame(HashSet<Player> players) {
        PokerGameValidator.validatePlayers(players);
        this.table = new Table(players);
    }

    public void start() {
        while (!table.existWinner()) {
            Round round = new Round(table);
            round.play();
            table.removeBrokePlayers();
        }
        System.out.println("Game over! Winner: " + table.getWinner().getName());
    }
}
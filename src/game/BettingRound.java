package game;

public class BettingRound {
    private final Table table;
    private final String name;

    public BettingRound(Table table, String name) {
        this.table = table;
        this.name = name;
    }

    public void play() {
        System.out.println("Starting betting round: " + name);
        for (Player player : table.getPlayers()) {
            if (!player.isFolded()) {
                int bet = Math.min(10, player.getCash());
                player.setCash(player.getCash() - bet);
                System.out.println(player.getName() + " bets " + bet);
            }
        }
    }
}

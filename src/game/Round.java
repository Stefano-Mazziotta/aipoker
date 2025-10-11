package game;

public class Round {
    private final Table table;

    public Round(Table table) {
        this.table = table;
    }

    public void play() {
        table.resetRound();
        table.getDeck().shuffle();
        dealHoleCards();

        // game steps
        BettingRound preFlop = new BettingRound(table, "Pre-Flop");
        preFlop.play();

        dealFlop();
        BettingRound flop = new BettingRound(table, "Flop");
        flop.play();

        dealTurn();
        BettingRound turn = new BettingRound(table, "Turn");
        turn.play();

        dealRiver();
        BettingRound river = new BettingRound(table, "River");
        river.play();

        determineWinner();
    }

    private void dealHoleCards() {
        for (int i = 0; i < 2; i++) {
            for (Player p : table.getPlayers()) {
                p.addCard(table.getDeck().dealCard());
            }
        }
    }

    private void dealFlop() {
        for (int i = 0; i < 3; i++)
            table.getCommunityCards().add(table.getDeck().dealCard());
    }

    private void dealTurn() {
        table.getCommunityCards().add(table.getDeck().dealCard());
    }

    private void dealRiver() {
        table.getCommunityCards().add(table.getDeck().dealCard());
    }

    private void determineWinner() {
        Player best = null;
        int bestScore = -1;
        for (Player p : table.getPlayers()) {
            if (!p.isFolded()) {
                int score = HandEvaluator.evaluateBestHand(p.getHoleCards(), table.getCommunityCards());
                if (score > bestScore) {
                    bestScore = score;
                    best = p;
                }
            }
        }
        table.setWinner(best);
        System.out.println("Winner: " + best.getName());
    }
}
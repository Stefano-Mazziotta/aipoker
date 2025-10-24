# Texas Hold'em Poker Hand Evaluation System

## 🏗️ Architecture Overview

This module implements a clean, extensible Texas Hold'em poker hand evaluation system following SOLID principles and OOP best practices.

## 📦 Core Components

### Interfaces

#### `HandEvaluationStrategy`
- **Purpose**: Strategy interface for different poker variants
- **Method**: `PokerHand evaluate(List<Card> cards)`
- **Usage**: Allows switching between Texas Hold'em, Omaha, etc.

#### `HandDetector`
- **Purpose**: Detects specific poker hand types
- **Method**: `Optional<PokerHand> detect(List<Card> cards)`
- **Pattern**: Chain of Responsibility
- **Implementations**: 
  - `StraightFlushDetector`
  - `FourOfAKindDetector`
  - `FullHouseDetector`
  - `FlushDetector`
  - `StraightDetector`
  - `ThreeOfAKindDetector`
  - `TwoPairDetector`
  - `OnePairDetector`
  - `HighCardDetector`

### Classes

#### `TexasHoldemEvaluator` (implements `HandEvaluationStrategy`)
- **Responsibility**: Orchestrates hand evaluation for Texas Hold'em
- **Process**:
  1. Validates 7 cards (2 hole + 5 community)
  2. Generates all 21 possible 5-card combinations (7C5)
  3. Evaluates each combination using detector chain
  4. Returns the best hand found

#### `PokerHand` (implements `Comparable<PokerHand>`)
- **Responsibility**: Immutable representation of a poker hand
- **Properties**:
  - `HandRank rank` - The type of hand
  - `List<Card> cards` - The 5 cards (immutable)
- **Comparison Logic**: Compares by rank first, then by kickers

#### `Card`
- **Responsibility**: Immutable representation of a playing card
- **Properties**:
  - `Rank rank` - Card rank (ACE to KING)
  - `Suit suit` - Card suit (HEARTS, DIAMONDS, CLUBS, SPADES)
- **Immutability**: Final fields, no setters

#### `CombinationUtils`
- **Responsibility**: Generate card combinations
- **Key Method**: `generateFiveCardCombinations(List<Card> cards)`
- **Algorithm**: Recursive combination generation (7C5 = 21 combinations)

### Enums

#### `HandRank`
- **Values** (descending order):
  1. `STRAIGHT_FLUSH` (9)
  2. `FOUR_OF_A_KIND` (8)
  3. `FULL_HOUSE` (7)
  4. `FLUSH` (6)
  5. `STRAIGHT` (5)
  6. `THREE_OF_A_KIND` (4)
  7. `TWO_PAIR` (3)
  8. `ONE_PAIR` (2)
  9. `HIGH_CARD` (1)

#### `Rank`
- Values: `ACE`, `TWO`, `THREE`, ..., `KING`
- Special handling: ACE can be high (14) or low (1) in straights

#### `Suit`
- Values: `HEARTS`, `DIAMONDS`, `CLUBS`, `SPADES`

## 🎯 SOLID Principles Applied

### Single Responsibility Principle (SRP)
- Each detector handles exactly one hand type
- `PokerHand` only represents a hand
- `CombinationUtils` only generates combinations
- `TexasHoldemEvaluator` only orchestrates evaluation

### Open/Closed Principle (OCP)
- Adding new hand types requires only creating new `HandDetector` implementations
- No modification of existing code needed
- Extensible through constructor injection in `TexasHoldemEvaluator`

### Liskov Substitution Principle (LSP)
- All `HandDetector` implementations are substitutable
- Different evaluation strategies can replace `TexasHoldemEvaluator`

### Interface Segregation Principle (ISP)
- Small, focused interfaces (`HandDetector`, `HandEvaluationStrategy`)
- Clients depend only on methods they use

### Dependency Inversion Principle (DIP)
- `TexasHoldemEvaluator` depends on `HandDetector` abstraction, not concrete implementations
- Easy to mock for testing

## 📊 Evaluation Flow

```
1. Player has 2 hole cards + 5 community cards (7 total)
2. TexasHoldemEvaluator.evaluate(cards)
3. Generate all 7C5 = 21 combinations
4. For each combination:
   a. Test with StraightFlushDetector
   b. Test with FourOfAKindDetector
   c. ... (continue down the chain)
   d. HighCardDetector always succeeds
5. Compare all detected hands
6. Return the best PokerHand
```

## 💡 Usage Examples

### Basic Evaluation
```java
List<Card> playerCards = List.of(
    new Card(Rank.ACE, Suit.SPADES),
    new Card(Rank.KING, Suit.SPADES)
);
List<Card> communityCards = List.of(
    new Card(Rank.QUEEN, Suit.SPADES),
    new Card(Rank.JACK, Suit.SPADES),
    new Card(Rank.TEN, Suit.SPADES),
    new Card(Rank.TWO, Suit.HEARTS),
    new Card(Rank.THREE, Suit.CLUBS)
);

List<Card> allCards = Stream.concat(
    playerCards.stream(), 
    communityCards.stream()
).toList();

TexasHoldemEvaluator evaluator = new TexasHoldemEvaluator();
PokerHand bestHand = evaluator.evaluate(allCards);
System.out.println("Best Hand: " + bestHand.getRank());
// Output: Best Hand: Straight Flush
```

### Hand Comparison
```java
PokerHand hand1 = evaluator.evaluate(cards1);
PokerHand hand2 = evaluator.evaluate(cards2);

int comparison = hand1.compareTo(hand2);
if (comparison > 0) {
    System.out.println("Hand 1 wins!");
} else if (comparison < 0) {
    System.out.println("Hand 2 wins!");
} else {
    System.out.println("It's a tie!");
}
```

### Integration with Game
```java
// In Table.java
public void determineWinner() {
    HandEvaluator handEval = new HandEvaluator();
    Player best = null;
    PokerHand bestHand = null;
    
    for (Player player : players) {
        if (!player.isFolded()) {
            PokerHand hand = handEval.evaluate(
                player.getHoleCards(), 
                communityCards
            );
            if (bestHand == null || hand.compareTo(bestHand) > 0) {
                bestHand = hand;
                best = player;
            }
        }
    }
    this.winner = best;
}
```

## 🧪 Testing Strategy

### Unit Tests for Each Detector
```java
@Test
public void testStraightFlushDetection() {
    List<Card> cards = List.of(
        new Card(Rank.TEN, Suit.HEARTS),
        new Card(Rank.NINE, Suit.HEARTS),
        new Card(Rank.EIGHT, Suit.HEARTS),
        new Card(Rank.SEVEN, Suit.HEARTS),
        new Card(Rank.SIX, Suit.HEARTS)
    );
    
    StraightFlushDetector detector = new StraightFlushDetector();
    Optional<PokerHand> result = detector.detect(cards);
    
    assertTrue(result.isPresent());
    assertEquals(HandRank.STRAIGHT_FLUSH, result.get().getRank());
}
```

### Integration Tests
```java
@Test
public void testBestHandSelection() {
    // Test that evaluator correctly selects best hand from 7 cards
    TexasHoldemEvaluator evaluator = new TexasHoldemEvaluator();
    List<Card> cards = /* 7 cards that could form multiple hands */;
    
    PokerHand bestHand = evaluator.evaluate(cards);
    assertEquals(HandRank.FLUSH, bestHand.getRank());
}
```

## 🔍 Special Cases Handled

### Ace-Low Straight (Wheel)
- Detects A-2-3-4-5 as a valid straight
- ACE is treated as low (value 1) in this case

### Tiebreaking
- Compares hands of same rank by kickers
- Example: A-A-K vs A-A-Q → First hand wins (King kicker beats Queen)

### Immutability
- All cards and hands are immutable
- Thread-safe operations
- No unexpected side effects

## 📁 File Structure

```
game/evaluation/
├── HandEvaluationStrategy.java    (Interface)
├── HandDetector.java               (Interface)
├── TexasHoldemEvaluator.java      (Main evaluator)
├── PokerHand.java                  (Hand representation)
├── HandRank.java                   (Enum)
├── CombinationUtils.java           (Utility)
├── EvaluationExample.java          (Usage examples)
└── detectors/
    ├── StraightFlushDetector.java
    ├── FourOfAKindDetector.java
    ├── FullHouseDetector.java
    ├── FlushDetector.java
    ├── StraightDetector.java
    ├── ThreeOfAKindDetector.java
    ├── TwoPairDetector.java
    ├── OnePairDetector.java
    └── HighCardDetector.java
```

## 🚀 Running the Example

Run the `EvaluationExample.java` main method to see various hand evaluations in action:

```bash
cd eclipse-workspace/aipoker/src
javac game/evaluation/EvaluationExample.java
java game.evaluation.EvaluationExample
```

## 🔄 Future Enhancements

1. **Royal Flush Detection**: Separate detector for 10-J-Q-K-A suited
2. **Performance Optimization**: Cache hand evaluations
3. **Additional Variants**: Omaha, Five-Card Draw evaluators
4. **Better Comparison**: Detailed comparison results (not just int)
5. **Hand Strength Calculation**: Probability-based hand strength
6. **Multi-way Pot Splitting**: Handle tie scenarios with pot division

## 📚 References

- [Poker Hand Rankings](https://www.cardschat.com/poker/strategy/poker-hands/)
- [SOLID Principles](https://en.wikipedia.org/wiki/SOLID)
- [Chain of Responsibility Pattern](https://refactoring.guru/design-patterns/chain-of-responsibility)

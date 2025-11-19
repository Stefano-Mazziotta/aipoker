---
description: 'Explain how a specific poker concept is implemented'
---

Explain how this poker concept is implemented in the codebase: ${input:concept:hand evaluation|betting|pot calculation|blinds|folding}

## What to Cover

1. **Domain Model** - Which classes/value objects represent this concept?
2. **Location** - Where in the codebase is it? (package path)
3. **Key Methods** - What are the main operations?
4. **Usage Example** - Show how to use it in code
5. **Rules Implemented** - What poker rules are enforced?

## Reference These Areas

- **Hand Evaluation**: `game/domain/evaluation/` - TexasHoldemEvaluator, detectors
- **Player Actions**: `player/domain/model/` - Player, PlayerAction, Chips
- **Game State**: `game/domain/model/` - Game, GameState, Round, BettingRound
- **Money**: `player/domain/model/Chips`, `game/domain/model/Pot`, `Blinds`
- **Cards**: `shared/domain/valueobject/` - Card, Rank, Suit, Deck

## Example Output Format

**Concept**: Hand Evaluation

**Implementation**:
- Main class: `TexasHoldemEvaluator` in `game/domain/evaluation/`
- Uses 9 detector classes in Chain of Responsibility pattern
- Generates all 21 combinations of 5 cards from 7 (2 hole + 5 community)

**Usage**:
```java
TexasHoldemEvaluator evaluator = new TexasHoldemEvaluator();
List<Card> allCards = /* 7 cards */;
PokerHand bestHand = evaluator.evaluate(allCards);
HandRank rank = bestHand.getRank(); // e.g., STRAIGHT_FLUSH
```

**Rules**:
- Compares hands by rank first (Straight Flush > Four of a Kind > ...)
- Handles tiebreakers with kicker comparison
- Supports Ace-low straights (A-2-3-4-5)

Provide similar explanation for the requested concept.

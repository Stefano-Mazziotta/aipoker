---
description: 'Add a new domain method to an existing entity'
---

Add a domain method to the entity in ${file}.

## Requirements

1. **Method should encapsulate business logic**, not just getters/setters
2. **Use domain language** - method names should reflect poker terminology
3. **Validate preconditions** - throw domain exceptions if invalid
4. **Maintain invariants** - ensure entity stays in valid state
5. **Return new immutable objects** for value objects
6. **Modify entity state** for entities

## Examples of Good Domain Methods

**Player entity**:
```java
public void bet(int amount) {
    if (this.folded) {
        throw new IllegalActionException("Cannot bet after folding");
    }
    if (!chips.canAfford(amount)) {
        throw new IllegalActionException("Insufficient chips");
    }
    this.chips = this.chips.subtract(amount);
}

public void receiveWinnings(Pot pot) {
    this.chips = this.chips.add(pot.getAmount());
}
```

**Game entity**:
```java
public void advanceToFlop() {
    if (state != GameState.PRE_FLOP) {
        throw new InvalidGameStateException("Cannot deal flop from state: " + state);
    }
    dealCommunityCards(3);
    this.state = GameState.FLOP;
}
```

## Bad Examples (Don't Do This)

❌ Just a setter:
```java
public void setChips(int chips) { this.chips = chips; }
```

❌ No validation:
```java
public void updateState(GameState state) { this.state = state; }
```

❌ Infrastructure concerns:
```java
public void saveToDatabase() { /* SQL code */ }
```

## Implementation Steps

1. Identify the business operation (what does the user/system want to do?)
2. Name the method using domain language (bet, fold, deal, evaluate)
3. Add precondition checks (throw exceptions if invalid)
4. Perform the operation (update state, create new objects)
5. Maintain invariants (ensure entity is still valid)

Add the domain method now following these guidelines.

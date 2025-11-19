---
description: 'Create a new use case in the application layer'
---

Create a new use case following these guidelines:

## Input Required
- Use case name: ${input:useCaseName:ActionUseCase}
- Feature: ${input:feature:game|player|lobby|ranking}

## Use Case Structure

### Location
`com.poker.${feature}/application/${useCaseName}.java`

### Requirements
1. Single responsibility - one use case, one action
2. Orchestrates domain logic, doesn't contain business rules
3. Uses repository interfaces (from domain layer)
4. Returns DTOs or domain objects
5. Handles application-level errors
6. Simple constructor injection for dependencies

### Template Pattern
```java
package com.poker.${feature}.application;

import com.poker.${feature}.domain.model.*;
import com.poker.${feature}.domain.repository.*;
import com.poker.shared.domain.exception.*;

public class ${useCaseName} {
    private final SomeRepository repository;
    // Add other dependencies
    
    public ${useCaseName}(SomeRepository repository) {
        this.repository = repository;
    }
    
    public ResultType execute(/* input parameters */) {
        // 1. Validate input (basic checks)
        validateInput(/* params */);
        
        // 2. Load domain objects from repositories
        SomeEntity entity = repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Entity not found"));
        
        // 3. Call domain methods (business logic)
        entity.domainAction(/* params */);
        
        // 4. Save changes
        repository.save(entity);
        
        // 5. Return result
        return entity; // or DTO
    }
    
    private void validateInput(/* params */) {
        if (/* invalid */) {
            throw new ValidationException("Invalid input");
        }
    }
}
```

### Common Use Case Patterns

**Command Use Case** (changes state):
- Returns void or entity
- Saves to repository
- Example: `RegisterPlayerUseCase`, `StartGameUseCase`

**Query Use Case** (reads data):
- Returns entity or DTO
- No repository saves
- Example: `GetPlayerStatsUseCase`, `GetLeaderboardUseCase`

**Event Use Case** (reacts to events):
- Triggered by domain events
- Updates multiple aggregates
- Example: `UpdateRankingAfterGameUseCase`

### Rules
- NO business logic (that goes in domain)
- NO infrastructure code (use repository interfaces)
- Keep it simple and focused
- Use domain exceptions, not application exceptions

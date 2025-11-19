---
description: 'Create a new domain entity following DDD patterns'
---

Create a new domain entity following these guidelines:

## Input Required
- Entity name: ${input:entityName:EntityName}
- Package: ${input:package:com.poker.feature.domain.model}

## Requirements

### Entity Structure
1. Should be an **Aggregate Root** or **Entity** (not a value object)
2. Must have an identity (e.g., `EntityNameId` value object)
3. Should validate its own invariants
4. Use immutable value objects for properties where appropriate
5. Provide factory methods for creation
6. Include domain methods (behavior, not just getters/setters)

### Value Object Requirements (if creating ID)
- Immutable class
- Based on UUID
- Factory methods: `generate()`, `from(String)`, `from(UUID)`
- Proper `equals()`, `hashCode()`, `toString()`

### Code Template Pattern
```java
package ${package};

import java.util.Objects;

public class ${entityName} {
    private final ${entityName}Id id;
    // ... other fields
    
    public ${entityName}(${entityName}Id id, /* other params */) {
        this.id = Objects.requireNonNull(id);
        // ... validate and assign
    }
    
    // Factory methods
    public static ${entityName} create(/* params */) {
        return new ${entityName}(${entityName}Id.generate(), /* ... */);
    }
    
    // Domain methods (not just getters)
    public void domainAction() {
        // Business logic here
    }
    
    // Getters
    public ${entityName}Id getId() { return id; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ${entityName})) return false;
        ${entityName} that = (${entityName}) o;
        return id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
```

### Rules
- No infrastructure dependencies (no database, no network)
- Throw domain exceptions (extend `DomainException`)
- Use value objects for concepts with validation
- Make it expressive (use domain language)

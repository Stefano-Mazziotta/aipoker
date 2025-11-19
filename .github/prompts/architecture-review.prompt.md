---
description: 'Review code for architectural compliance'
---

Review ${file} for architectural compliance with Hexagonal Architecture and DDD principles.

## Check These Rules

### Domain Layer (domain/*)
- ✅ NO infrastructure dependencies (no java.sql, no java.net, no file I/O)
- ✅ Only depends on other domain classes or java.util
- ✅ Value objects are immutable
- ✅ Entities have identity and rich behavior
- ✅ Business logic is in domain methods, not getters/setters
- ✅ Exceptions extend DomainException
- ✅ Repository interfaces (no implementations)

### Application Layer (application/*)
- ✅ Use cases orchestrate domain logic
- ✅ NO business rules (those go in domain)
- ✅ Depends on domain layer only (repositories as interfaces)
- ✅ Simple input validation
- ✅ Transaction boundaries

### Infrastructure Layer (infrastructure/*)
- ✅ Implements repository interfaces from domain
- ✅ Contains database, network, file system code
- ✅ Maps between domain objects and external formats
- ✅ Can depend on domain layer
- ✅ NO domain logic here

### General
- ✅ Package structure follows features (game/, player/, lobby/)
- ✅ Immutability where appropriate
- ✅ Meaningful names using poker terminology
- ✅ Proper exception handling

## Output Format

Return findings in this format:

**✅ Compliant**
- List what follows the rules correctly

**⚠️ Warnings**
- List potential issues or improvements

**❌ Violations**
- List clear rule violations that must be fixed

**Recommendations**
- Suggest improvements

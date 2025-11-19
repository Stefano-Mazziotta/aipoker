---
description: 'Create a repository interface and SQLite implementation'
---

Create a repository following the Repository pattern:

## Input Required
- Aggregate name: ${input:aggregateName:Entity}
- Feature: ${input:feature:game|player|lobby|ranking}

## Repository Structure

### 1. Interface (Port) - Domain Layer
**Location**: `com.poker.${feature}/domain/repository/${aggregateName}Repository.java`

```java
package com.poker.${feature}.domain.repository;

import com.poker.${feature}.domain.model.${aggregateName};
import com.poker.${feature}.domain.model.${aggregateName}Id;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ${aggregateName} aggregate.
 * Port in Hexagonal Architecture.
 */
public interface ${aggregateName}Repository {
    
    void save(${aggregateName} entity);
    
    Optional<${aggregateName}> findById(${aggregateName}Id id);
    
    List<${aggregateName}> findAll();
    
    void delete(${aggregateName}Id id);
    
    boolean exists(${aggregateName}Id id);
    
    // Add specific queries as needed
}
```

### 2. Implementation (Adapter) - Infrastructure Layer
**Location**: `com.poker.${feature}/infrastructure/persistence/SQLite${aggregateName}Repository.java`

```java
package com.poker.${feature}.infrastructure.persistence;

import com.poker.${feature}.domain.model.*;
import com.poker.${feature}.domain.repository.${aggregateName}Repository;
import java.sql.*;
import java.util.*;

public class SQLite${aggregateName}Repository implements ${aggregateName}Repository {
    private final Connection connection;
    
    public SQLite${aggregateName}Repository(Connection connection) {
        this.connection = connection;
    }
    
    @Override
    public void save(${aggregateName} entity) {
        String sql = "INSERT OR REPLACE INTO table_name (id, field1, field2) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, entity.getId().toString());
            // Set other fields
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save ${aggregateName}", e);
        }
    }
    
    @Override
    public Optional<${aggregateName}> findById(${aggregateName}Id id) {
        String sql = "SELECT * FROM table_name WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id.toString());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapToEntity(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find ${aggregateName}", e);
        }
    }
    
    @Override
    public List<${aggregateName}> findAll() {
        List<${aggregateName}> entities = new ArrayList<>();
        String sql = "SELECT * FROM table_name";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                entities.add(mapToEntity(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all", e);
        }
        return entities;
    }
    
    @Override
    public void delete(${aggregateName}Id id) {
        String sql = "DELETE FROM table_name WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete", e);
        }
    }
    
    @Override
    public boolean exists(${aggregateName}Id id) {
        String sql = "SELECT COUNT(*) FROM table_name WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id.toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check existence", e);
        }
    }
    
    private ${aggregateName} mapToEntity(ResultSet rs) throws SQLException {
        // Map database columns to domain entity
        // Use factory method or reconstitute method
        return ${aggregateName}.reconstitute(
            ${aggregateName}Id.from(rs.getString("id")),
            // other fields
        );
    }
}
```

## Requirements
1. Interface in domain layer (NO SQL, NO Connection)
2. Implementation in infrastructure layer
3. Map between database and domain objects
4. Handle SQL exceptions gracefully
5. Use PreparedStatement to prevent SQL injection
6. Return Optional for single results
7. Use entity's reconstitute/factory methods

## Database Table Reference
Check `schema.sql` for table structure.

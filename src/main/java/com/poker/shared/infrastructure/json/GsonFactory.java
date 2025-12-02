package com.poker.shared.infrastructure.json;

import java.time.Instant;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

/**
 * Factory for creating configured Gson instances with custom type adapters.
 * Centralizes Gson configuration to ensure consistency across the application.
 */
public class GsonFactory {
    
    private static final Gson INSTANCE = createGson();
    
    private GsonFactory() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Returns a shared Gson instance with custom type adapters configured.
     */
    public static Gson getInstance() {
        return INSTANCE;
    }
    
    /**
     * Creates a new Gson instance with custom type adapters.
     * Use this if you need a separate instance with the same configuration.
     */
    public static Gson createGson() {
        return new GsonBuilder()
            .registerTypeAdapter(Instant.class, 
                (JsonSerializer<Instant>) (src, typeOfSrc, context) -> 
                    new com.google.gson.JsonPrimitive(src.toString()))
            .registerTypeAdapter(Instant.class, 
                (JsonDeserializer<Instant>) (json, typeOfT, context) -> 
                    Instant.parse(json.getAsString()))
            .create();
    }
}

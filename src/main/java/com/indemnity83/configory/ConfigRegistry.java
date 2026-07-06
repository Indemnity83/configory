package com.indemnity83.configory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Process-wide registry mapping each mod id to a single shared {@link Config} instance.
 *
 * <p>Ensures every part of a mod (and Configory's own host helpers) sees the same config for a given
 * id. Lookups are thread-safe and create the config on first access.
 */
public final class ConfigRegistry {
    private static final Map<String, Config> CONFIGS = new ConcurrentHashMap<>();

    private ConfigRegistry() {}

    /**
     * {@return the shared config for the given id, creating a file-backed one on first access}
     *
     * @param id the config/mod id
     */
    public static Config config(String id) {
        return CONFIGS.computeIfAbsent(id, Config::create);
    }

    /**
     * {@return the shared config for the given id, creating it if necessary}
     *
     * <p>Synonym for {@link #config(String)}.
     *
     * @param id the config/mod id
     */
    public static Config getOrCreate(String id) {
        return config(id);
    }
}

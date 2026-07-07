package com.indemnity83.configory;

import com.indemnity83.configory.storage.ConfigStorage;
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

    /**
     * {@return the shared config for the given id, creating one backed by {@code storage} on first
     * access}
     *
     * <p>The {@code storage} is used only when the config is created; if one already exists for the
     * id (from an earlier call, with any storage), that instance is returned unchanged. Register the
     * storage before the id is first accessed — e.g. in tests or when a loader supplies the config
     * directory.
     *
     * @param id the config/mod id
     * @param storage the storage to back a newly created config
     */
    public static Config config(String id, ConfigStorage storage) {
        return CONFIGS.computeIfAbsent(id, key -> Config.create(key, storage));
    }

    /**
     * {@return the shared config for the given id, creating one backed by {@code storage} if
     * necessary}
     *
     * <p>Synonym for {@link #config(String, ConfigStorage)}.
     *
     * @param id the config/mod id
     * @param storage the storage to back a newly created config
     */
    public static Config getOrCreate(String id, ConfigStorage storage) {
        return config(id, storage);
    }
}

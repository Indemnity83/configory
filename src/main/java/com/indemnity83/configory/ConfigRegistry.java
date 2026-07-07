package com.indemnity83.configory;

import com.indemnity83.configory.storage.ConfigStorage;
import java.util.ArrayList;
import java.util.List;
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

    /**
     * {@return every registered config nested under {@code modId} — ids starting with
     * {@code modId + "."}; the main config for {@code modId} itself is not included}
     *
     * <p>Used by the bootstrap convention to load a mod's whole config tree. The trailing dot keeps
     * one mod's children from matching another whose id merely shares a prefix.
     *
     * @param modId the mod id whose child configs to collect
     */
    public static List<Config> childConfigs(String modId) {
        String prefix = modId + ".";
        List<Config> children = new ArrayList<>();
        for (Config config : CONFIGS.values()) {
            if (config.id().startsWith(prefix)) {
                children.add(config);
            }
        }
        return children;
    }
}

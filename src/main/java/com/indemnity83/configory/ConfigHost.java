package com.indemnity83.configory;

/**
 * Convenience mix-in implemented by a mod's entry point to work with its config directly.
 *
 * <p>Implementing this interface gives a mod class ready-made {@code getConfig} / {@code setConfig} /
 * {@code saveConfig} / {@code reloadConfig} helpers that resolve to the mod's own {@link Config}
 * instance. When no explicit config id is supplied, the id is discovered from a
 * {@code public static final String MOD_ID} (or {@code MODID}) field on the implementing class.
 *
 * <p>The intended pattern is to keep config key constants in a nested class named {@code Configs}
 * (extending {@link ConfigEntries}) and call {@link #bootstrapConfig(String)} once during mod init.
 */
public interface ConfigHost {
    /**
     * Initializes this host's config for the given id: forces the nested {@code Configs} class to
     * load (registering its keys), runs its optional {@code bootstrap(Config)} hook, then loads,
     * defaults, validates, and sanitizes the config files.
     *
     * @param configId the config/mod id to bootstrap
     * @throws ConfigException if no nested {@code Configs} class is found or bootstrap fails
     */
    default void bootstrapConfig(String configId) {
        Config config = ConfigRegistry.getOrCreate(configId);
        ConfigBootstrap.bootstrap(getClass(), config);
    }

    /**
     * Bootstraps this host's config using the id resolved from its {@code MOD_ID}/{@code MODID}
     * field.
     *
     * @throws ConfigException if the id cannot be resolved or bootstrap fails
     * @see #bootstrapConfig(String)
     */
    default void bootstrapConfig() {
        bootstrapConfig(ConfigHostSupport.resolveModId(getClass()));
    }

    /**
     * {@return the shared {@link Config} for the given id, creating it if necessary}
     *
     * @param configId the config/mod id
     */
    default Config config(String configId) {
        return ConfigRegistry.getOrCreate(configId);
    }

    /**
     * {@return this host's own {@link Config}, resolved from its {@code MOD_ID}/{@code MODID} field}
     *
     * @throws ConfigException if the id cannot be resolved from this class
     */
    default Config config() {
        return ConfigRegistry.getOrCreate(ConfigHostSupport.resolveModId(getClass()));
    }

    /**
     * Reads the raw value at a path from this host's config for dynamic, untyped access.
     *
     * @param path a dotted path or bare key
     * @return a {@link ConfigValue} wrapping the stored value (possibly absent)
     * @see Config#get(String)
     */
    default ConfigValue getConfig(String path) {
        return config().get(path);
    }

    /**
     * Reads the typed value for a key from this host's config, returning its default when unset.
     *
     * @param key a key registered with this host's config
     * @param <T> the value type
     * @return the stored value, or the definition's default
     * @see Config#get(ConfigKey)
     */
    default <T> T getConfig(ConfigKey<T> key) {
        return config().get(key);
    }

    /**
     * Writes a value at a path in this host's config in memory (no validation, not saved to disk).
     *
     * @param path a dotted path or bare key
     * @param value a supported primitive value
     * @return a {@link ConfigMutation} for optionally chaining {@code save()}
     * @see Config#set(String, Object)
     */
    default ConfigMutation setConfig(String path, Object value) {
        return config().set(path, value);
    }

    /**
     * Writes and validates a typed value for a key in this host's config (not saved to disk).
     *
     * @param key a key registered with this host's config
     * @param value the value to validate and store
     * @param <T> the value type
     * @return a {@link ConfigMutation} for optionally chaining {@code save()}
     * @see Config#set(ConfigKey, Object)
     */
    default <T> ConfigMutation setConfig(ConfigKey<T> key, T value) {
        return config().set(key, value);
    }

    /**
     * Saves all dirty files in this host's config to disk.
     *
     * @see Config#save()
     */
    default void saveConfig() {
        config().save();
    }

    /**
     * Reloads this host's config from disk, refusing to discard unsaved changes.
     *
     * @throws ConfigException if there are unsaved changes
     * @see Config#reload()
     */
    default void reloadConfig() {
        config().reload();
    }
}

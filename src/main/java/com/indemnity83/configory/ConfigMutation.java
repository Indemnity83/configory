package com.indemnity83.configory;

/**
 * The result of a {@code set} call, describing what happened and offering a chained save.
 *
 * <p>Returned by {@link Config#set(String, Object)} / {@link Config#set(ConfigKey, Object)}. A
 * mutation only records an in-memory change; call {@link #save()} to persist the config to disk.
 */
public final class ConfigMutation {
    private final Config config;
    private final ConfigPath path;
    private final boolean changed;

    /**
     * Creates a mutation result. Normally returned by a {@code set} call rather than constructed
     * directly.
     *
     * @param config the owning config
     * @param path the path that was written
     * @param changed whether the new value differed from the previous one
     */
    public ConfigMutation(Config config, ConfigPath path, boolean changed) {
        this.config = config;
        this.path = path;
        this.changed = changed;
    }

    /**
     * {@return whether the set actually changed the stored value}
     *
     * <p>Returns {@code false} when the new value equalled the existing one, in which case the config
     * was not marked dirty.
     */
    public boolean changed() {
        return changed;
    }

    /**
     * {@return the path that was written}
     */
    public ConfigPath path() {
        return path;
    }

    /**
     * Saves the config to disk.
     *
     * @return this mutation, for further chaining
     */
    public ConfigMutation save() {
        config.save();
        return this;
    }
}

package com.indemnity83.configory;

import java.util.Objects;

/**
 * A typed, type-safe handle to a single registered config value.
 *
 * <p>Keys are produced by the fluent builder's {@code register()} and are meant to be stored as
 * {@code public static final} constants. Passing a key to {@link Config#get(ConfigKey)} or
 * {@link Config#set(ConfigKey, Object)} gives compile-time type safety and avoids string typos. A
 * key remembers which config it belongs to, so using it against a different config is rejected.
 *
 * @param <T> the value type this key reads and writes
 */
public final class ConfigKey<T> {
    private final String configId;
    private final ConfigDefinition<T> definition;

    /**
     * Creates a key bound to a config id and its definition. Usually obtained from the builder's
     * {@code register()} rather than constructed directly.
     *
     * @param configId the id of the owning config
     * @param definition the definition describing the value's path, type, default, and constraints
     */
    public ConfigKey(String configId, ConfigDefinition<T> definition) {
        this.configId = Objects.requireNonNull(configId, "configId");
        this.definition = Objects.requireNonNull(definition, "definition");
    }

    /**
     * {@return the id of the config this key belongs to}
     */
    public String configId() {
        return configId;
    }

    /**
     * {@return the path of the value this key addresses}
     */
    public ConfigPath path() {
        return definition.path();
    }

    /**
     * {@return the definition backing this key (type, default value, description, constraints)}
     */
    public ConfigDefinition<T> definition() {
        return definition;
    }

    /**
     * {@return a debug string of the form {@code <configId>:<file>.<segments>}}
     */
    @Override
    public String toString() {
        return configId + ":" + path().fullPath();
    }
}

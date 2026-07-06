package com.indemnity83.configory.builder;

import com.indemnity83.configory.Config;
import com.indemnity83.configory.ConfigPath;

/**
 * The first stage of the fluent definition chain, where the value's type is chosen.
 *
 * <p>Obtained from {@link Config#define(String)}. Selecting a type ({@code asFloat()},
 * {@code asInt()}, ...) returns a typed builder on which you set a default value, optional
 * constraints, and finally call {@code register()} to obtain a {@link com.indemnity83.configory.ConfigKey}.
 */
public final class ConfigDefinitionBuilder {
    private final Config config;
    private final ConfigPath path;

    /**
     * Creates a builder for the given path. Normally obtained from {@link Config#define(String)}.
     *
     * @param config the config the resulting key will belong to
     * @param path the path being defined
     */
    public ConfigDefinitionBuilder(Config config, ConfigPath path) {
        this.config = config;
        this.path = path;
    }

    /**
     * {@return a builder that types this value as a boolean}
     */
    public BooleanConfigBuilder asBoolean() {
        return new BooleanConfigBuilder(config, path);
    }

    /**
     * {@return a builder that types this value as a string}
     */
    public StringConfigBuilder asString() {
        return new StringConfigBuilder(config, path);
    }

    /**
     * {@return a builder that types this value as an int}
     */
    public IntConfigBuilder asInt() {
        return new IntConfigBuilder(config, path);
    }

    /**
     * {@return a builder that types this value as a long}
     */
    public LongConfigBuilder asLong() {
        return new LongConfigBuilder(config, path);
    }

    /**
     * {@return a builder that types this value as a float}
     */
    public FloatConfigBuilder asFloat() {
        return new FloatConfigBuilder(config, path);
    }

    /**
     * {@return a builder that types this value as a double}
     */
    public DoubleConfigBuilder asDouble() {
        return new DoubleConfigBuilder(config, path);
    }
}

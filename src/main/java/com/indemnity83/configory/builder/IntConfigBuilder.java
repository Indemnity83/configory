package com.indemnity83.configory.builder;

import com.indemnity83.configory.Config;
import com.indemnity83.configory.ConfigPath;
import com.indemnity83.configory.ConfigType;

/**
 * Fluent builder for an {@code int} config value, with numeric bound constraints.
 *
 * <p>Obtained via {@code define(path).asInt()}.
 */
public final class IntConfigBuilder extends NumericConfigBuilder<Integer, IntConfigBuilder> {
    /**
     * Creates an int builder. Normally obtained via {@code define(path).asInt()}.
     *
     * @param config the config the resulting key will belong to
     * @param path the path being defined
     */
    public IntConfigBuilder(Config config, ConfigPath path) {
        super(config, path, ConfigType.INT, Integer.class);
    }

    @Override
    protected IntConfigBuilder self() {
        return this;
    }
}

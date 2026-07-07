package com.indemnity83.configory.builder;

import com.indemnity83.configory.Config;
import com.indemnity83.configory.ConfigPath;
import com.indemnity83.configory.ConfigType;

/**
 * Fluent builder for a {@code double} config value, with numeric bound constraints.
 *
 * <p>Obtained via {@code define(path).asDouble()}.
 */
public final class DoubleConfigBuilder extends FloatingPointConfigBuilder<Double, DoubleConfigBuilder> {
    /**
     * Creates a double builder. Normally obtained via {@code define(path).asDouble()}.
     *
     * @param config the config the resulting key will belong to
     * @param path the path being defined
     */
    public DoubleConfigBuilder(Config config, ConfigPath path) {
        super(config, path, ConfigType.DOUBLE, Double.class);
    }

    @Override
    protected DoubleConfigBuilder self() {
        return this;
    }
}

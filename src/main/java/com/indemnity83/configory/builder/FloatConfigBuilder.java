package com.indemnity83.configory.builder;

import com.indemnity83.configory.Config;
import com.indemnity83.configory.ConfigPath;
import com.indemnity83.configory.ConfigType;

/**
 * Fluent builder for a {@code float} config value, with numeric bound constraints.
 *
 * <p>Obtained via {@code define(path).asFloat()}.
 */
public final class FloatConfigBuilder extends NumericConfigBuilder<Float, FloatConfigBuilder> {
    /**
     * Creates a float builder. Normally obtained via {@code define(path).asFloat()}.
     *
     * @param config the config the resulting key will belong to
     * @param path the path being defined
     */
    public FloatConfigBuilder(Config config, ConfigPath path) {
        super(config, path, ConfigType.FLOAT, Float.class);
    }

    @Override
    protected FloatConfigBuilder self() {
        return this;
    }
}

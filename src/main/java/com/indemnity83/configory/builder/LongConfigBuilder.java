package com.indemnity83.configory.builder;

import com.indemnity83.configory.Config;
import com.indemnity83.configory.ConfigPath;
import com.indemnity83.configory.ConfigType;

/**
 * Fluent builder for a {@code long} config value, with numeric bound constraints.
 *
 * <p>Obtained via {@code define(path).asLong()}.
 */
public final class LongConfigBuilder extends NumericConfigBuilder<Long, LongConfigBuilder> {
    /**
     * Creates a long builder. Normally obtained via {@code define(path).asLong()}.
     *
     * @param config the config the resulting key will belong to
     * @param path the path being defined
     */
    public LongConfigBuilder(Config config, ConfigPath path) {
        super(config, path, ConfigType.LONG, Long.class);
    }

    @Override
    protected LongConfigBuilder self() {
        return this;
    }
}

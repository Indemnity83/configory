package com.indemnity83.configory.builder;

import com.indemnity83.configory.Config;
import com.indemnity83.configory.ConfigPath;
import com.indemnity83.configory.ConfigType;

/**
 * Fluent builder for a boolean config value.
 *
 * <p>Obtained via {@code define(path).asBoolean()}. Supports the common default, description, and
 * custom-validator steps; booleans have no additional built-in constraints.
 */
public final class BooleanConfigBuilder extends BaseConfigBuilder<Boolean, BooleanConfigBuilder> {
    /**
     * Creates a boolean builder. Normally obtained via {@code define(path).asBoolean()}.
     *
     * @param config the config the resulting key will belong to
     * @param path the path being defined
     */
    public BooleanConfigBuilder(Config config, ConfigPath path) {
        super(config, path, ConfigType.BOOLEAN, Boolean.class);
    }

    @Override
    protected BooleanConfigBuilder self() {
        return this;
    }
}

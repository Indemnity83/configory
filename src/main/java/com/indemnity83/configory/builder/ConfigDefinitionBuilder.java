package com.indemnity83.configory.builder;

import com.indemnity83.configory.Config;
import com.indemnity83.configory.ConfigPath;

public final class ConfigDefinitionBuilder {
    private final Config config;
    private final ConfigPath path;

    public ConfigDefinitionBuilder(Config config, ConfigPath path) {
        this.config = config;
        this.path = path;
    }

    public BooleanConfigBuilder asBoolean() {
        return new BooleanConfigBuilder(config, path);
    }

    public StringConfigBuilder asString() {
        return new StringConfigBuilder(config, path);
    }

    public IntConfigBuilder asInt() {
        return new IntConfigBuilder(config, path);
    }

    public LongConfigBuilder asLong() {
        return new LongConfigBuilder(config, path);
    }

    public FloatConfigBuilder asFloat() {
        return new FloatConfigBuilder(config, path);
    }

    public DoubleConfigBuilder asDouble() {
        return new DoubleConfigBuilder(config, path);
    }
}

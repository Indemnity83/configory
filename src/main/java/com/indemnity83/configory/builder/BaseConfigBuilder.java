package com.indemnity83.configory.builder;

import com.indemnity83.configory.*;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseConfigBuilder<T, SELF extends BaseConfigBuilder<T, SELF>> {
    protected final Config config;
    protected final ConfigPath path;
    protected final ConfigType type;
    protected final Class<T> valueClass;
    protected T defaultValue;
    protected String description = "";
    protected final List<ConfigConstraint<T>> constraints = new ArrayList<>();

    protected BaseConfigBuilder(Config config, ConfigPath path, ConfigType type, Class<T> valueClass) {
        this.config = config;
        this.path = path;
        this.type = type;
        this.valueClass = valueClass;
    }

    protected abstract SELF self();

    public SELF defaultValue(T value) {
        this.defaultValue = value;
        return self();
    }

    public SELF defaultsTo(T value) {
        return defaultValue(value);
    }

    public SELF describe(String description) {
        this.description = description;
        return self();
    }

    public SELF validator(ConfigConstraint<T> constraint) {
        this.constraints.add(constraint);
        return self();
    }

    public ConfigKey<T> register() {
        if (defaultValue == null) {
            throw new ConfigException("Config key " + path.fullPath() + " is missing a default value.");
        }
        ConfigDefinition<T> definition =
                new ConfigDefinition<>(path, type, valueClass, defaultValue, description, constraints);
        return config.registerDefinition(definition);
    }
}

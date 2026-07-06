package com.indemnity83.configory;

import java.util.Objects;

public final class ConfigKey<T> {
    private final String configId;
    private final ConfigDefinition<T> definition;

    public ConfigKey(String configId, ConfigDefinition<T> definition) {
        this.configId = Objects.requireNonNull(configId, "configId");
        this.definition = Objects.requireNonNull(definition, "definition");
    }

    public String configId() {
        return configId;
    }

    public ConfigPath path() {
        return definition.path();
    }

    public ConfigDefinition<T> definition() {
        return definition;
    }

    @Override
    public String toString() {
        return configId + ":" + path().fullPath();
    }
}

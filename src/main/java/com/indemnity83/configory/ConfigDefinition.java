package com.indemnity83.configory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ConfigDefinition<T> {
    private final ConfigPath path;
    private final ConfigType type;
    private final Class<T> valueClass;
    private final T defaultValue;
    private final String description;
    private final List<ConfigConstraint<T>> constraints;

    public ConfigDefinition(
            ConfigPath path,
            ConfigType type,
            Class<T> valueClass,
            T defaultValue,
            String description,
            List<ConfigConstraint<T>> constraints) {
        this.path = Objects.requireNonNull(path, "path");
        this.type = Objects.requireNonNull(type, "type");
        this.valueClass = Objects.requireNonNull(valueClass, "valueClass");
        this.defaultValue = Objects.requireNonNull(defaultValue, "defaultValue");
        this.description = description == null ? "" : description;
        this.constraints = List.copyOf(new ArrayList<>(constraints));
    }

    public ConfigPath path() {
        return path;
    }

    public ConfigType type() {
        return type;
    }

    public Class<T> valueClass() {
        return valueClass;
    }

    public T defaultValue() {
        return defaultValue;
    }

    public String description() {
        return description;
    }

    public List<ConfigConstraint<T>> constraints() {
        return constraints;
    }

    public ValidationResult validate(T value, Config config) {
        for (ConfigConstraint<T> constraint : constraints) {
            ValidationResult result = constraint.validate(value, config);
            if (!result.valid()) {
                return result;
            }
        }
        return ValidationResult.ok();
    }
}

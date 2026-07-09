package com.indemnity83.configory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The immutable specification of a single config value: its path, type, default, description, and
 * validation constraints.
 *
 * <p>Definitions are normally produced by the fluent builder chain and registered to obtain a
 * {@link ConfigKey}. They also drive load-time behavior: applying defaults, validating stored
 * values, and describing keys to tooling.
 *
 * @param <T> the value type
 */
public final class ConfigDefinition<T> {
    private final ConfigPath path;
    private final ConfigType type;
    private final Class<T> valueClass;
    private final T defaultValue;
    private final String description;
    private final boolean exposed;
    private final List<ConfigConstraint<T>> constraints;
    private final List<ConfigPath> formerPaths;

    /**
     * Creates a definition. Constraints are defensively copied.
     *
     * @param path the value's path
     * @param type the value's {@link ConfigType}
     * @param valueClass the Java class of the value
     * @param defaultValue the value used when the key is unset or the stored value is invalid; must
     *     not be null
     * @param description a human-readable description for tooling; a null description becomes empty
     * @param exposed whether this value is included in a generated command surface (the default;
     *     unset by {@code hidden()})
     * @param constraints the validation constraints applied in order
     * @param formerPaths paths this key was formerly stored at; searched in order when the primary
     *     path is absent, and stripped from the document on every load
     */
    public ConfigDefinition(
            ConfigPath path,
            ConfigType type,
            Class<T> valueClass,
            T defaultValue,
            String description,
            boolean exposed,
            List<ConfigConstraint<T>> constraints,
            List<ConfigPath> formerPaths) {
        this.path = Objects.requireNonNull(path, "path");
        this.type = Objects.requireNonNull(type, "type");
        this.valueClass = Objects.requireNonNull(valueClass, "valueClass");
        this.defaultValue = Objects.requireNonNull(defaultValue, "defaultValue");
        this.description = description == null ? "" : description;
        this.exposed = exposed;
        this.constraints = List.copyOf(new ArrayList<>(constraints));
        this.formerPaths = List.copyOf(new ArrayList<>(formerPaths));
    }

    /**
     * {@return the path this value is stored at}
     */
    public ConfigPath path() {
        return path;
    }

    /**
     * {@return the declared config type of this value}
     */
    public ConfigType type() {
        return type;
    }

    /**
     * {@return the Java class of this value}
     */
    public Class<T> valueClass() {
        return valueClass;
    }

    /**
     * {@return the value used when the key is unset or a stored value is invalid}
     */
    public T defaultValue() {
        return defaultValue;
    }

    /**
     * {@return the human-readable description, or an empty string if none was given}
     */
    public String description() {
        return description;
    }

    /**
     * {@return whether this value is exposed to a generated command surface}
     *
     * <p>True by default; {@code hidden()} opts a key out.
     *
     * @see com.indemnity83.configory.builder.BaseConfigBuilder#hidden()
     */
    public boolean isExposed() {
        return exposed;
    }

    /**
     * {@return the immutable list of validation constraints, applied in order}
     */
    public List<ConfigConstraint<T>> constraints() {
        return constraints;
    }

    /**
     * {@return the paths this key was formerly stored at, in search order}
     *
     * <p>When the {@linkplain #path() primary path} is absent on load, these are searched in order and
     * the first present value that coerces and validates is adopted at the primary path. Every former
     * path is then stripped from the document on each load, whether or not a value was migrated.
     */
    public List<ConfigPath> formerPaths() {
        return formerPaths;
    }

    /**
     * Runs each constraint in order and returns the first failure, or success if all pass.
     *
     * @param value the candidate value to validate
     * @param config the config for constraints that reference other values
     * @return the first failing {@link ValidationResult}, or {@link ValidationResult#ok()} if valid
     */
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

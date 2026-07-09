package com.indemnity83.configory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

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

    /**
     * A place a key's value is migrated from when its primary path is unset: either a former
     * {@code path} in the same document, or a {@code supplier} that computes the value. Exactly one is
     * non-null.
     *
     * @param path the former dotted path, or null for a supplier source
     * @param supplier the value producer, or null for a path source
     * @param <T> the value type
     */
    public record FormerSource<T>(ConfigPath path, Supplier<T> supplier) {
        public static <T> FormerSource<T> ofPath(ConfigPath path) {
            return new FormerSource<>(path, null);
        }

        public static <T> FormerSource<T> ofSupplier(Supplier<T> supplier) {
            return new FormerSource<>(null, supplier);
        }
    }

    private final ConfigPath path;
    private final ConfigType type;
    private final Class<T> valueClass;
    private final T defaultValue;
    private final String description;
    private final boolean exposed;
    private final List<ConfigConstraint<T>> constraints;
    private final List<FormerSource<T>> formerSources;

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
     * @param formerSources places this key's value is migrated from when the primary path is unset;
     *     tried in order, and any path sources are stripped from the document on every load
     */
    public ConfigDefinition(
            ConfigPath path,
            ConfigType type,
            Class<T> valueClass,
            T defaultValue,
            String description,
            boolean exposed,
            List<ConfigConstraint<T>> constraints,
            List<FormerSource<T>> formerSources) {
        this.path = Objects.requireNonNull(path, "path");
        this.type = Objects.requireNonNull(type, "type");
        this.valueClass = Objects.requireNonNull(valueClass, "valueClass");
        this.defaultValue = Objects.requireNonNull(defaultValue, "defaultValue");
        this.description = description == null ? "" : description;
        this.exposed = exposed;
        this.constraints = List.copyOf(new ArrayList<>(constraints));
        this.formerSources = List.copyOf(new ArrayList<>(formerSources));
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
     * {@return the places this key's value is migrated from when unset, in search order}
     *
     * <p>When the {@linkplain #path() primary path} is absent on load, these sources are tried in
     * order and the first that yields a value passing validation is adopted at the primary path. Any
     * path source is then stripped from the document on each load, whether or not a value was
     * migrated; supplier sources have nothing to strip.
     */
    public List<FormerSource<T>> formerSources() {
        return formerSources;
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

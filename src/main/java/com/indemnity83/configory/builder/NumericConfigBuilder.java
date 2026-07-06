package com.indemnity83.configory.builder;

import com.indemnity83.configory.*;
import java.util.function.Supplier;

/**
 * Fluent builder shared by all numeric types, adding bound constraints on top of
 * {@link BaseConfigBuilder}.
 *
 * <p>Beyond fixed {@link #min}/{@link #max}/{@link #range} bounds, the {@code *ValueOf} variants
 * bind a bound to another config key, evaluated at validation time via a supplier so cross-references
 * work regardless of static initialization order.
 *
 * @param <T> the numeric, comparable value type
 * @param <SELF> the concrete builder subtype, for fluent chaining
 */
public abstract class NumericConfigBuilder<T extends Number & Comparable<T>, SELF extends NumericConfigBuilder<T, SELF>>
        extends BaseConfigBuilder<T, SELF> {

    /**
     * Creates a numeric builder for a specific type.
     *
     * @param config the config the resulting key will belong to
     * @param path the path being defined
     * @param type the config type
     * @param valueClass the Java class of the value
     */
    protected NumericConfigBuilder(Config config, ConfigPath path, ConfigType type, Class<T> valueClass) {
        super(config, path, type, valueClass);
    }

    /**
     * Constrains the value to be at least {@code min} (inclusive).
     *
     * @param min the inclusive lower bound
     * @return this builder
     */
    public SELF min(T min) {
        constraints.add((value, config) -> value.compareTo(min) < 0
                ? ValidationResult.error("Value must be at least " + min + ".")
                : ValidationResult.ok());
        return self();
    }

    /**
     * Constrains the value to be at most {@code max} (inclusive).
     *
     * @param max the inclusive upper bound
     * @return this builder
     */
    public SELF max(T max) {
        constraints.add((value, config) -> value.compareTo(max) > 0
                ? ValidationResult.error("Value must be at most " + max + ".")
                : ValidationResult.ok());
        return self();
    }

    /**
     * Constrains the value to the inclusive range {@code [min, max]}.
     *
     * @param min the inclusive lower bound
     * @param max the inclusive upper bound
     * @return this builder
     */
    public SELF range(T min, T max) {
        return min(min).max(max);
    }

    /**
     * Constrains the value to be at least the current value of another key.
     *
     * <p>The referenced key is resolved lazily through the supplier at validation time, so it may be
     * declared after this one. Use for cross-field invariants such as a max that must not fall below
     * its paired min.
     *
     * @param keySupplier supplies the key whose current value is the inclusive lower bound
     * @return this builder
     */
    public SELF minValueOf(Supplier<ConfigKey<T>> keySupplier) {
        constraints.add((value, config) -> {
            ConfigKey<T> key = keySupplier.get();
            T min = config.get(key);
            return value.compareTo(min) < 0
                    ? ValidationResult.error(
                            "Value must be at least " + key.path().fullPath() + " (" + min + ").")
                    : ValidationResult.ok();
        });
        return self();
    }

    /**
     * Constrains the value to be at most the current value of another key.
     *
     * <p>The referenced key is resolved lazily through the supplier at validation time, so it may be
     * declared after this one. Use for cross-field invariants such as a min that must not exceed its
     * paired max.
     *
     * @param keySupplier supplies the key whose current value is the inclusive upper bound
     * @return this builder
     */
    public SELF maxValueOf(Supplier<ConfigKey<T>> keySupplier) {
        constraints.add((value, config) -> {
            ConfigKey<T> key = keySupplier.get();
            T max = config.get(key);
            return value.compareTo(max) > 0
                    ? ValidationResult.error(
                            "Value must be at most " + key.path().fullPath() + " (" + max + ").")
                    : ValidationResult.ok();
        });
        return self();
    }
}

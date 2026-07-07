package com.indemnity83.configory.builder;

import com.indemnity83.configory.Config;
import com.indemnity83.configory.ConfigPath;
import com.indemnity83.configory.ConfigType;
import com.indemnity83.configory.ValidationResult;

/**
 * Fluent builder shared by the floating-point types, adding constraints that only make sense for
 * {@code float}/{@code double} on top of {@link NumericConfigBuilder}.
 *
 * @param <T> the floating-point value type
 * @param <SELF> the concrete builder subtype, for fluent chaining
 */
public abstract class FloatingPointConfigBuilder<
                T extends Number & Comparable<T>, SELF extends FloatingPointConfigBuilder<T, SELF>>
        extends NumericConfigBuilder<T, SELF> {

    /**
     * Creates a floating-point builder for a specific type.
     *
     * @param config the config the resulting key will belong to
     * @param path the path being defined
     * @param type the config type
     * @param valueClass the Java class of the value
     */
    protected FloatingPointConfigBuilder(Config config, ConfigPath path, ConfigType type, Class<T> valueClass) {
        super(config, path, type, valueClass);
    }

    /**
     * Constrains the value to be finite, rejecting {@code NaN} and infinities.
     *
     * @return this builder
     */
    public SELF finite() {
        constraints.add((value, config) -> Double.isFinite(value.doubleValue())
                ? ValidationResult.ok()
                : ValidationResult.error("Value must be finite."));
        return self();
    }
}

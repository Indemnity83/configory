package com.indemnity83.configory;

/**
 * A single validation rule applied to a config value.
 *
 * <p>Constraints are attached to a definition via the builder (for example {@code min}, {@code max},
 * {@code allowedValues}, or {@code validator}) and are run in order on load and on every
 * {@code set}. The {@code config} argument lets a constraint reference other values for cross-field
 * validation.
 *
 * @param <T> the value type being validated
 */
@FunctionalInterface
public interface ConfigConstraint<T> {
    /**
     * Validates a candidate value.
     *
     * @param value the value to check
     * @param config the owning config, for constraints that depend on other values
     * @return {@link ValidationResult#ok()} if acceptable, otherwise a
     *     {@link ValidationResult#error(String)} explaining the failure
     */
    ValidationResult validate(T value, Config config);
}

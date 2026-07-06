package com.indemnity83.configory;

/**
 * The outcome of validating a config value: either success, or failure with a message.
 *
 * <p>Returned by {@link ConfigConstraint#validate(Object, Config)} and
 * {@link ConfigDefinition#validate(Object, Config)}. Construct instances with the {@link #ok()} and
 * {@link #error(String)} factories.
 *
 * @param valid whether the value passed validation
 * @param message a human-readable explanation of the failure, or an empty string when valid
 */
public record ValidationResult(boolean valid, String message) {
    /**
     * {@return a successful result with an empty message}
     */
    public static ValidationResult ok() {
        return new ValidationResult(true, "");
    }

    /**
     * {@return a failed result carrying the given explanation}
     *
     * @param message a human-readable reason the value was rejected
     */
    public static ValidationResult error(String message) {
        return new ValidationResult(false, message);
    }
}

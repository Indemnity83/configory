package com.indemnity83.configory;

/**
 * Thrown when a config value fails validation.
 *
 * <p>Raised by {@link Config#set(ConfigKey, Object)} when the supplied value violates a constraint,
 * and by {@link Config#get(ConfigKey)} when the value stored on disk is invalid. It is a
 * {@link ConfigException}, so catching the parent type also handles validation failures.
 */
public class ConfigValidationException extends ConfigException {
    /**
     * Creates an exception with the given detail message.
     *
     * @param message the detail message describing the validation failure
     */
    public ConfigValidationException(String message) {
        super(message);
    }
}

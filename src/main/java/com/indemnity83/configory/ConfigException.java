package com.indemnity83.configory;

/**
 * Unchecked exception for Configory failures such as invalid paths, duplicate definitions,
 * unsupported value types, storage I/O errors, and bootstrap problems.
 *
 * <p>Being a {@link RuntimeException}, it does not need to be declared. Validation-specific failures
 * are reported via the {@link ConfigValidationException} subclass.
 */
public class ConfigException extends RuntimeException {
    /**
     * Creates an exception with the given detail message.
     *
     * @param message the detail message
     */
    public ConfigException(String message) {
        super(message);
    }

    /**
     * Creates an exception with the given detail message and cause.
     *
     * @param message the detail message
     * @param cause the underlying cause
     */
    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}

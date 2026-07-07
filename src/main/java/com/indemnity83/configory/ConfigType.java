package com.indemnity83.configory;

/**
 * The set of primitive value types Configory can store and validate.
 *
 * <p>Each type determines how a value is coerced to and from JSON and which Java class a
 * {@link ConfigKey} exposes. Choosing a type is the first step of the fluent builder chain
 * ({@code asBoolean()}, {@code asString()}, {@code asInt()}, and so on).
 */
public enum ConfigType {
    /** A {@code boolean} value. */
    BOOLEAN,
    /** A {@code String} value. */
    STRING,
    /** A 32-bit signed integer ({@code int}). */
    INT,
    /** A 64-bit signed integer ({@code long}). */
    LONG,
    /** A single-precision floating-point value ({@code float}). */
    FLOAT,
    /** A double-precision floating-point value ({@code double}). */
    DOUBLE,
    /** An {@link Enum} constant, stored by its {@code name()}. */
    ENUM
}

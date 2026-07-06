package com.indemnity83.configory;

import com.google.gson.JsonElement;

/**
 * The raw value read from a path via {@link Config#get(String)}, coerced on demand by its
 * {@code asX} accessors.
 *
 * <p>This is the untyped, dynamic counterpart to {@link ConfigKey}-based access and is convenient
 * for commands, debug tools, and generated screens. Each strict accessor coerces the underlying JSON
 * to the requested type and throws {@link ConfigException} on a type mismatch, missing value, or out
 * of range number; the fallback overloads instead return the supplied default in those cases. Call
 * {@link #isPresent()} to test for a value before a strict read. Unlike
 * {@link Config#get(ConfigKey)}, no definition constraints are applied here.
 */
public final class ConfigValue {
    private final Config config;
    private final ConfigPath path;
    private final JsonElement element;

    /**
     * Wraps the raw JSON element read from a path. Usually obtained via {@link Config#get(String)}
     * rather than constructed directly.
     *
     * @param config the owning config
     * @param path the path this value was read from
     * @param element the raw JSON element, or null if nothing is stored at the path
     */
    public ConfigValue(Config config, ConfigPath path, JsonElement element) {
        this.config = config;
        this.path = path;
        this.element = element;
    }

    /**
     * {@return whether a value is present at this path}
     *
     * <p>Presence means a non-null JSON value is stored here; it does not guarantee the value coerces
     * to any particular type. Use this to branch before a strict {@code asX} read instead of catching
     * {@link ConfigException} or supplying a fallback.
     */
    public boolean isPresent() {
        return element != null && !element.isJsonNull();
    }

    /**
     * {@return whether no value is present at this path}
     *
     * @see #isPresent()
     */
    public boolean isEmpty() {
        return !isPresent();
    }

    /**
     * {@return the value as a boolean}
     *
     * @throws ConfigException if the value is missing or not a boolean
     */
    public boolean asBoolean() {
        return read(ConfigType.BOOLEAN);
    }

    /**
     * {@return the value as a boolean, or {@code fallback} if it is missing or not a boolean}
     *
     * @param fallback the value to return when coercion fails
     */
    public boolean asBoolean(boolean fallback) {
        return readOrFallback(ConfigType.BOOLEAN, fallback);
    }

    /**
     * {@return the value as a string}
     *
     * @throws ConfigException if the value is missing or not a string
     */
    public String asString() {
        return read(ConfigType.STRING);
    }

    /**
     * {@return the value as a string, or {@code fallback} if it is missing or not a string}
     *
     * @param fallback the value to return when coercion fails
     */
    public String asString(String fallback) {
        return readOrFallback(ConfigType.STRING, fallback);
    }

    /**
     * {@return the value as an int}
     *
     * @throws ConfigException if the value is missing, not a whole number, or out of int range
     */
    public int asInt() {
        return read(ConfigType.INT);
    }

    /**
     * {@return the value as an int, or {@code fallback} if coercion fails}
     *
     * @param fallback the value to return when coercion fails
     */
    public int asInt(int fallback) {
        return readOrFallback(ConfigType.INT, fallback);
    }

    /**
     * {@return the value as a long}
     *
     * @throws ConfigException if the value is missing or not a whole number
     */
    public long asLong() {
        return read(ConfigType.LONG);
    }

    /**
     * {@return the value as a long, or {@code fallback} if coercion fails}
     *
     * @param fallback the value to return when coercion fails
     */
    public long asLong(long fallback) {
        return readOrFallback(ConfigType.LONG, fallback);
    }

    /**
     * {@return the value as a float}
     *
     * @throws ConfigException if the value is missing or not a number
     */
    public float asFloat() {
        return read(ConfigType.FLOAT);
    }

    /**
     * {@return the value as a float, or {@code fallback} if coercion fails}
     *
     * @param fallback the value to return when coercion fails
     */
    public float asFloat(float fallback) {
        return readOrFallback(ConfigType.FLOAT, fallback);
    }

    /**
     * {@return the value as a double}
     *
     * @throws ConfigException if the value is missing or not a number
     */
    public double asDouble() {
        return read(ConfigType.DOUBLE);
    }

    /**
     * {@return the value as a double, or {@code fallback} if coercion fails}
     *
     * @param fallback the value to return when coercion fails
     */
    public double asDouble(double fallback) {
        return readOrFallback(ConfigType.DOUBLE, fallback);
    }

    private <T> T read(ConfigType type) {
        try {
            return ConfigValues.fromJson(element, type);
        } catch (ConfigException e) {
            throw new ConfigException("Invalid config value at " + path.fullPath() + ": " + e.getMessage(), e);
        }
    }

    private <T> T readOrFallback(ConfigType type, T fallback) {
        try {
            return ConfigValues.fromJson(element, type);
        } catch (ConfigException ignored) {
            return fallback;
        }
    }
}

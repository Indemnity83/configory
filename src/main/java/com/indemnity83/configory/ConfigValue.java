package com.indemnity83.configory;

import com.google.gson.JsonElement;

public final class ConfigValue {
    private final Config config;
    private final ConfigPath path;
    private final JsonElement element;

    public ConfigValue(Config config, ConfigPath path, JsonElement element) {
        this.config = config;
        this.path = path;
        this.element = element;
    }

    public boolean asBoolean() {
        return read(ConfigType.BOOLEAN);
    }

    public boolean asBoolean(boolean fallback) {
        return readOrFallback(ConfigType.BOOLEAN, fallback);
    }

    public String asString() {
        return read(ConfigType.STRING);
    }

    public String asString(String fallback) {
        return readOrFallback(ConfigType.STRING, fallback);
    }

    public int asInt() {
        return read(ConfigType.INT);
    }

    public int asInt(int fallback) {
        return readOrFallback(ConfigType.INT, fallback);
    }

    public long asLong() {
        return read(ConfigType.LONG);
    }

    public long asLong(long fallback) {
        return readOrFallback(ConfigType.LONG, fallback);
    }

    public float asFloat() {
        return read(ConfigType.FLOAT);
    }

    public float asFloat(float fallback) {
        return readOrFallback(ConfigType.FLOAT, fallback);
    }

    public double asDouble() {
        return read(ConfigType.DOUBLE);
    }

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

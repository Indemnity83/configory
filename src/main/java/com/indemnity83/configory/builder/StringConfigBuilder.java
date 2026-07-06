package com.indemnity83.configory.builder;

import com.indemnity83.configory.Config;
import com.indemnity83.configory.ConfigPath;
import com.indemnity83.configory.ConfigType;
import com.indemnity83.configory.ValidationResult;
import java.util.Set;

/**
 * Fluent builder for a string config value.
 *
 * <p>Obtained via {@code define(path).asString()}. Adds {@link #allowedValues(String...)} on top of
 * the common default, description, and custom-validator steps.
 */
public final class StringConfigBuilder extends BaseConfigBuilder<String, StringConfigBuilder> {
    /**
     * Creates a string builder. Normally obtained via {@code define(path).asString()}.
     *
     * @param config the config the resulting key will belong to
     * @param path the path being defined
     */
    public StringConfigBuilder(Config config, ConfigPath path) {
        super(config, path, ConfigType.STRING, String.class);
    }

    @Override
    protected StringConfigBuilder self() {
        return this;
    }

    /**
     * Constrains the value to one of an allowed set (an enumeration).
     *
     * @param values the permitted values
     * @return this builder
     */
    public StringConfigBuilder allowedValues(String... values) {
        Set<String> allowed = Set.of(values);
        constraints.add((value, config) -> allowed.contains(value)
                ? ValidationResult.ok()
                : ValidationResult.error("Value must be one of " + allowed + "."));
        return this;
    }
}

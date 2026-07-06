package com.indemnity83.configory.builder;

import com.indemnity83.configory.Config;
import com.indemnity83.configory.ConfigPath;
import com.indemnity83.configory.ConfigType;
import com.indemnity83.configory.ValidationResult;
import java.util.Set;

public final class StringConfigBuilder extends BaseConfigBuilder<String, StringConfigBuilder> {
    public StringConfigBuilder(Config config, ConfigPath path) {
        super(config, path, ConfigType.STRING, String.class);
    }

    @Override
    protected StringConfigBuilder self() {
        return this;
    }

    public StringConfigBuilder allowedValues(String... values) {
        Set<String> allowed = Set.of(values);
        constraints.add((value, config) -> allowed.contains(value)
                ? ValidationResult.ok()
                : ValidationResult.error("Value must be one of " + allowed + "."));
        return this;
    }
}

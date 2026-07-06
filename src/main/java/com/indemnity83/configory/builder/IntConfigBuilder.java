package com.indemnity83.configory.builder;

import com.indemnity83.configory.Config;
import com.indemnity83.configory.ConfigPath;
import com.indemnity83.configory.ConfigType;

public final class IntConfigBuilder extends NumericConfigBuilder<Integer, IntConfigBuilder> {
    public IntConfigBuilder(Config config, ConfigPath path) {
        super(config, path, ConfigType.INT, Integer.class);
    }

    @Override
    protected IntConfigBuilder self() {
        return this;
    }
}

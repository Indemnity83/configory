package com.indemnity83.configory.builder;

import com.indemnity83.configory.Config;
import com.indemnity83.configory.ConfigPath;
import com.indemnity83.configory.ConfigType;

public final class FloatConfigBuilder extends NumericConfigBuilder<Float, FloatConfigBuilder> {
    public FloatConfigBuilder(Config config, ConfigPath path) {
        super(config, path, ConfigType.FLOAT, Float.class);
    }

    @Override
    protected FloatConfigBuilder self() {
        return this;
    }
}

package com.indemnity83.configory.builder;

import com.indemnity83.configory.Config;
import com.indemnity83.configory.ConfigPath;
import com.indemnity83.configory.ConfigType;

public final class DoubleConfigBuilder extends NumericConfigBuilder<Double, DoubleConfigBuilder> {
    public DoubleConfigBuilder(Config config, ConfigPath path) {
        super(config, path, ConfigType.DOUBLE, Double.class);
    }

    @Override
    protected DoubleConfigBuilder self() {
        return this;
    }
}

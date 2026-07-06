package com.indemnity83.configory.builder;

import com.indemnity83.configory.Config;
import com.indemnity83.configory.ConfigPath;
import com.indemnity83.configory.ConfigType;

public final class BooleanConfigBuilder extends BaseConfigBuilder<Boolean, BooleanConfigBuilder> {
    public BooleanConfigBuilder(Config config, ConfigPath path) {
        super(config, path, ConfigType.BOOLEAN, Boolean.class);
    }

    @Override
    protected BooleanConfigBuilder self() {
        return this;
    }
}

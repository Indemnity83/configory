package com.indemnity83.configory.builder;

import com.indemnity83.configory.Config;
import com.indemnity83.configory.ConfigPath;
import com.indemnity83.configory.ConfigType;

public final class LongConfigBuilder extends NumericConfigBuilder<Long, LongConfigBuilder> {
    public LongConfigBuilder(Config config, ConfigPath path) {
        super(config, path, ConfigType.LONG, Long.class);
    }

    @Override
    protected LongConfigBuilder self() {
        return this;
    }
}

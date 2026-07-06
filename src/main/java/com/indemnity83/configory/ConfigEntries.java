package com.indemnity83.configory;

public abstract class ConfigEntries {
    protected static Config configFor(String configId) {
        return ConfigRegistry.getOrCreate(configId);
    }

    protected static Config configFor(Class<?> hostClass) {
        return ConfigRegistry.getOrCreate(ConfigHostSupport.resolveModId(hostClass));
    }
}

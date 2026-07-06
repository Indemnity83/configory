package com.indemnity83.configory;

public interface ConfigHost {
    default void bootstrapConfig(String configId) {
        Config config = ConfigRegistry.getOrCreate(configId);
        ConfigBootstrap.bootstrap(getClass(), config);
    }

    default void bootstrapConfig() {
        bootstrapConfig(ConfigHostSupport.resolveModId(getClass()));
    }

    default Config config(String configId) {
        return ConfigRegistry.getOrCreate(configId);
    }

    default Config config() {
        return ConfigRegistry.getOrCreate(ConfigHostSupport.resolveModId(getClass()));
    }

    default ConfigValue getConfig(String path) {
        return config().get(path);
    }

    default <T> T getConfig(ConfigKey<T> key) {
        return config().get(key);
    }

    default ConfigMutation setConfig(String path, Object value) {
        return config().set(path, value);
    }

    default <T> ConfigMutation setConfig(ConfigKey<T> key, T value) {
        return config().set(key, value);
    }

    default void saveConfig() {
        config().save();
    }

    default void reloadConfig() {
        config().reload();
    }
}

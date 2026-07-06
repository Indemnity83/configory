package com.indemnity83.configory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ConfigRegistry {
    private static final Map<String, Config> CONFIGS = new ConcurrentHashMap<>();

    private ConfigRegistry() {}

    public static Config config(String id) {
        return CONFIGS.computeIfAbsent(id, Config::create);
    }

    public static Config getOrCreate(String id) {
        return config(id);
    }
}

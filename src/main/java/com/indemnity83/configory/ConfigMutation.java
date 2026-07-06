package com.indemnity83.configory;

public final class ConfigMutation {
    private final Config config;
    private final ConfigPath path;
    private final boolean changed;

    public ConfigMutation(Config config, ConfigPath path, boolean changed) {
        this.config = config;
        this.path = path;
        this.changed = changed;
    }

    public boolean changed() {
        return changed;
    }

    public ConfigPath path() {
        return path;
    }

    public ConfigMutation save() {
        config.save(path.file());
        return this;
    }
}

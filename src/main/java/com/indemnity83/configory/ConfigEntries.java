package com.indemnity83.configory;

/**
 * Base class for a mod's nested {@code Configs} holder of {@link ConfigKey} constants.
 *
 * <p>Extend this in a nested class named {@code Configs} inside your {@link ConfigHost}, obtain the
 * mod's {@link Config} via {@link #configFor(String)} (or {@link #configFor(Class)}), and declare
 * {@code public static final ConfigKey<...>} fields using {@code config.define(...)....register()}.
 * The bootstrap convention discovers this class by name and forces its static fields to initialize.
 */
public abstract class ConfigEntries {
    /**
     * {@return the shared {@link Config} for the given id, creating it if necessary}
     *
     * @param configId the config/mod id
     */
    protected static Config configFor(String configId) {
        return ConfigRegistry.getOrCreate(configId);
    }

    /**
     * {@return the shared {@link Config} for the id resolved from the host class's
     * {@code MOD_ID}/{@code MODID} field}
     *
     * @param hostClass the host class to resolve the mod id from
     * @throws ConfigException if the id cannot be resolved from the class
     */
    protected static Config configFor(Class<?> hostClass) {
        return ConfigRegistry.getOrCreate(ConfigHostSupport.resolveModId(hostClass));
    }
}

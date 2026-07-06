package com.indemnity83.configory;

import java.lang.reflect.Method;

public final class ConfigBootstrap {
    private ConfigBootstrap() {}

    public static void bootstrap(Class<?> hostClass, Config config) {
        Class<?> entriesClass = findConfigEntriesClass(hostClass);
        forceInitialize(entriesClass);
        invokeOptionalBootstrap(entriesClass, config);
        config.load();
    }

    private static Class<?> findConfigEntriesClass(Class<?> hostClass) {
        for (Class<?> nested : hostClass.getDeclaredClasses()) {
            if (nested.getSimpleName().equals("Configs")) {
                return nested;
            }
        }
        throw new ConfigException("Expected nested config class named Configs in " + hostClass.getName());
    }

    private static void forceInitialize(Class<?> type) {
        try {
            Class.forName(type.getName(), true, type.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new ConfigException("Unable to initialize config class " + type.getName(), e);
        }
    }

    private static void invokeOptionalBootstrap(Class<?> entriesClass, Config config) {
        try {
            Method method = entriesClass.getDeclaredMethod("bootstrap", Config.class);
            method.setAccessible(true);
            method.invoke(null, config);
        } catch (NoSuchMethodException noOptionalHook) {
        } catch (ReflectiveOperationException e) {
            throw new ConfigException("Failed to run config bootstrap for " + entriesClass.getName(), e);
        }
    }
}

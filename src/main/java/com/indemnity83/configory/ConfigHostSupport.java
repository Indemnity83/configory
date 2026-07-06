package com.indemnity83.configory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Optional;

public final class ConfigHostSupport {
    private ConfigHostSupport() {}

    public static String resolveModId(Class<?> hostClass) {
        return findStaticStringField(hostClass, "MOD_ID")
                .or(() -> findStaticStringField(hostClass, "MODID"))
                .orElseThrow(() -> new ConfigException("Unable to resolve config id for " + hostClass.getName()
                        + ". Pass bootstrapConfig(modId), define public static final String MOD_ID, "
                        + "or override with explicit ConfigRegistry usage."));
    }

    private static Optional<String> findStaticStringField(Class<?> type, String name) {
        try {
            Field field = type.getDeclaredField(name);
            int modifiers = field.getModifiers();
            if (!Modifier.isStatic(modifiers) || field.getType() != String.class) {
                return Optional.empty();
            }
            field.setAccessible(true);
            Object value = field.get(null);
            if (value instanceof String string && !string.isBlank()) {
                return Optional.of(string);
            }
            return Optional.empty();
        } catch (NoSuchFieldException ignored) {
            return Optional.empty();
        } catch (IllegalAccessException e) {
            throw new ConfigException("Unable to read " + name + " from " + type.getName(), e);
        }
    }
}

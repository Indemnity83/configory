package com.indemnity83.configory;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Converts between the supported config value types and their Gson JSON representation.
 *
 * <p>Internal to Configory; not part of the public API.
 */
final class ConfigValues {
    private ConfigValues() {}

    static JsonElement toJson(Object value) {
        if (value instanceof Boolean b) return new JsonPrimitive(b);
        if (value instanceof String s) return new JsonPrimitive(s);
        if (value instanceof Integer i) return new JsonPrimitive(i);
        if (value instanceof Long l) return new JsonPrimitive(l);
        if (value instanceof Float f) return new JsonPrimitive(f);
        if (value instanceof Double d) return new JsonPrimitive(d);
        throw new ConfigException(
                "Unsupported config value type: " + value.getClass().getName());
    }

    @SuppressWarnings("unchecked")
    static <T> T fromJson(JsonElement element, ConfigType type) {
        JsonPrimitive primitive = requirePrimitive(element);
        try {
            return switch (type) {
                case BOOLEAN -> {
                    if (!primitive.isBoolean()) throw new ConfigException("Expected boolean.");
                    yield (T) Boolean.valueOf(primitive.getAsBoolean());
                }
                case STRING -> {
                    if (!primitive.isString()) throw new ConfigException("Expected string.");
                    yield (T) primitive.getAsString();
                }
                case INT -> {
                    requireNumber(primitive, "int");
                    double number = requireWholeNumber(primitive);
                    if (number < Integer.MIN_VALUE || number > Integer.MAX_VALUE) {
                        throw new ConfigException("Int value out of range.");
                    }
                    yield (T) Integer.valueOf((int) number);
                }
                case LONG -> {
                    requireNumber(primitive, "long");
                    requireWholeNumber(primitive);
                    yield (T) Long.valueOf(primitive.getAsLong());
                }
                case FLOAT -> {
                    requireNumber(primitive, "float");
                    yield (T) Float.valueOf(primitive.getAsFloat());
                }
                case DOUBLE -> {
                    requireNumber(primitive, "double");
                    yield (T) Double.valueOf(primitive.getAsDouble());
                }
            };
        } catch (NumberFormatException e) {
            throw new ConfigException("Invalid numeric config value.", e);
        }
    }

    private static JsonPrimitive requirePrimitive(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            throw new ConfigException("Config value is missing.");
        }
        if (!element.isJsonPrimitive()) {
            throw new ConfigException("Config value must be a primitive.");
        }
        return element.getAsJsonPrimitive();
    }

    private static void requireNumber(JsonPrimitive primitive, String typeName) {
        if (!primitive.isNumber()) {
            throw new ConfigException("Expected " + typeName + ".");
        }
    }

    private static double requireWholeNumber(JsonPrimitive primitive) {
        double number = primitive.getAsDouble();
        if (number % 1 != 0) {
            throw new ConfigException("Expected whole number.");
        }
        return number;
    }
}

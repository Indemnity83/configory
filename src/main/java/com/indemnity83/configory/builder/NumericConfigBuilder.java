package com.indemnity83.configory.builder;

import com.indemnity83.configory.*;
import java.util.function.Supplier;

public abstract class NumericConfigBuilder<T extends Number & Comparable<T>, SELF extends NumericConfigBuilder<T, SELF>>
        extends BaseConfigBuilder<T, SELF> {

    protected NumericConfigBuilder(Config config, ConfigPath path, ConfigType type, Class<T> valueClass) {
        super(config, path, type, valueClass);
    }

    public SELF min(T min) {
        constraints.add((value, config) -> value.compareTo(min) < 0
                ? ValidationResult.error("Value must be at least " + min + ".")
                : ValidationResult.ok());
        return self();
    }

    public SELF max(T max) {
        constraints.add((value, config) -> value.compareTo(max) > 0
                ? ValidationResult.error("Value must be at most " + max + ".")
                : ValidationResult.ok());
        return self();
    }

    public SELF range(T min, T max) {
        return min(min).max(max);
    }

    public SELF minValueOf(Supplier<ConfigKey<T>> keySupplier) {
        constraints.add((value, config) -> {
            ConfigKey<T> key = keySupplier.get();
            T min = config.get(key);
            return value.compareTo(min) < 0
                    ? ValidationResult.error(
                            "Value must be at least " + key.path().fullPath() + " (" + min + ").")
                    : ValidationResult.ok();
        });
        return self();
    }

    public SELF maxValueOf(Supplier<ConfigKey<T>> keySupplier) {
        constraints.add((value, config) -> {
            ConfigKey<T> key = keySupplier.get();
            T max = config.get(key);
            return value.compareTo(max) > 0
                    ? ValidationResult.error(
                            "Value must be at most " + key.path().fullPath() + " (" + max + ").")
                    : ValidationResult.ok();
        });
        return self();
    }
}

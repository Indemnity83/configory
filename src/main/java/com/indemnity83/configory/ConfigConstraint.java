package com.indemnity83.configory;

@FunctionalInterface
public interface ConfigConstraint<T> {
    ValidationResult validate(T value, Config config);
}

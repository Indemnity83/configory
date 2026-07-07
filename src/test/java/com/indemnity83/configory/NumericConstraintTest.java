package com.indemnity83.configory;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class NumericConstraintTest {

    @Test
    void greaterThanRejectsBoundaryAndBelow() {
        Config config = Config.create("numeric", new InMemoryConfigStorage());
        ConfigKey<Double> speed =
                config.defineDouble("speed", 1.0).greaterThan(0.0).register();
        config.load();

        ConfigValidationException ex = assertThrows(ConfigValidationException.class, () -> config.set(speed, 0.0));
        assertTrue(ex.getMessage().contains("greater than"));
        assertThrows(ConfigValidationException.class, () -> config.set(speed, -1.0));
        assertDoesNotThrow(() -> config.set(speed, 0.5));
    }

    @Test
    void lessThanRejectsBoundaryAndAbove() {
        Config config = Config.create("numeric", new InMemoryConfigStorage());
        ConfigKey<Integer> count = config.defineInt("count", 1).lessThan(10).register();
        config.load();

        assertThrows(ConfigValidationException.class, () -> config.set(count, 10));
        assertDoesNotThrow(() -> config.set(count, 9));
    }

    @Test
    void finiteRejectsNaNAndInfinity() {
        Config config = Config.create("numeric", new InMemoryConfigStorage());
        ConfigKey<Float> rate = config.defineFloat("rate", 1.0f).finite().register();
        config.load();

        assertThrows(ConfigValidationException.class, () -> config.set(rate, Float.NaN));
        assertThrows(ConfigValidationException.class, () -> config.set(rate, Float.POSITIVE_INFINITY));
        assertDoesNotThrow(() -> config.set(rate, 2.5f));
    }
}

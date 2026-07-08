package com.indemnity83.configory;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class ConfigCrossFieldTest {

    @Test
    void repairMinMaxClampsMinDownToMax() {
        InMemoryConfigStorage storage = new InMemoryConfigStorage();
        Config config = Config.create("engines", storage);
        ConfigKey<Double> min = config.define("engines.min")
                .asDouble()
                .defaultValue(0.0)
                .min(0.0)
                .register();
        ConfigKey<Double> max = config.define("engines.max")
                .asDouble()
                .defaultValue(0.0)
                .min(0.0)
                .register();

        JsonObject doc = new JsonObject();
        JsonPaths.set(doc, ConfigPath.parse("engines.min"), new JsonPrimitive(8.0));
        JsonPaths.set(doc, ConfigPath.parse("engines.max"), new JsonPrimitive(5.0));
        storage.seed("engines", doc);

        config.load();
        assertEquals(8.0, config.get(min));
        assertEquals(5.0, config.get(max));

        config.repairMinMax(min, max);
        assertEquals(5.0, config.get(min), "min should be clamped down to max");
        assertEquals(5.0, config.get(max));
    }

    @Test
    void repairMinMaxLeavesConsistentValuesAlone() {
        InMemoryConfigStorage storage = new InMemoryConfigStorage();
        Config config = Config.create("engines", storage);
        ConfigKey<Double> min = config.define("engines.min")
                .asDouble()
                .defaultValue(3.0)
                .min(0.0)
                .register();
        ConfigKey<Double> max = config.define("engines.max")
                .asDouble()
                .defaultValue(10.0)
                .min(0.0)
                .register();
        config.load();

        config.repairMinMax(min, max);
        assertEquals(3.0, config.get(min));
        assertEquals(10.0, config.get(max));
    }

    @Test
    void repairMinMaxHealsInvertedPairWithCrossFieldValidators() {
        InMemoryConfigStorage storage = new InMemoryConfigStorage();
        Config config = Config.create("engines", storage);
        AtomicReference<ConfigKey<Double>> maxRef = new AtomicReference<>();
        AtomicReference<ConfigKey<Double>> minRef = new AtomicReference<>();
        ConfigKey<Double> min = config.define("stirling.min_output")
                .asDouble()
                .defaultValue(3.0)
                .min(0.0)
                .maxValueOf(maxRef::get)
                .register();
        ConfigKey<Double> max = config.define("stirling.max_output")
                .asDouble()
                .defaultValue(10.0)
                .min(0.0)
                .minValueOf(minRef::get)
                .register();
        minRef.set(min);
        maxRef.set(max);

        // Force an inverted pair via raw (unvalidated) sets, as a hand-edited file would.
        config.set("stirling.min_output", 12.0);
        config.set("stirling.max_output", 10.0);

        // Regression (#51): the validating read used to throw here instead of repairing.
        assertDoesNotThrow(() -> config.repairMinMax(min, max));
        assertEquals(10.0, config.get(min), "min clamped down to max");
        assertEquals(10.0, config.get(max));
    }

    @Test
    void sanitizeHookRunsOnLoad() {
        InMemoryConfigStorage storage = new InMemoryConfigStorage();
        Config config = Config.create("engines", storage);
        ConfigKey<Double> min = config.define("engines.min")
                .asDouble()
                .defaultValue(0.0)
                .min(0.0)
                .register();
        ConfigKey<Double> max = config.define("engines.max")
                .asDouble()
                .defaultValue(0.0)
                .min(0.0)
                .register();
        config.registerSanitizeHook(() -> config.repairMinMax(min, max));

        JsonObject doc = new JsonObject();
        JsonPaths.set(doc, ConfigPath.parse("engines.min"), new JsonPrimitive(8.0));
        JsonPaths.set(doc, ConfigPath.parse("engines.max"), new JsonPrimitive(5.0));
        storage.seed("engines", doc);

        config.load();
        // The sanitize hook ran as part of load(), so min was already clamped.
        assertEquals(5.0, config.get(min));
    }

    @Test
    void crossFieldConstraintReferencesAnotherKey() {
        InMemoryConfigStorage storage = new InMemoryConfigStorage();
        Config config = Config.create("engines", storage);
        // engines.min must not exceed engines.max
        ConfigKey<Double> max = config.define("engines.max")
                .asDouble()
                .defaultValue(10.0)
                .min(0.0)
                .register();
        ConfigKey<Double> min = config.define("engines.min")
                .asDouble()
                .defaultValue(3.0)
                .min(0.0)
                .maxValueOf(() -> max)
                .register();
        config.load();

        assertDoesNotThrow(() -> config.set(min, 7.0));
        ConfigValidationException ex = assertThrows(ConfigValidationException.class, () -> config.set(min, 12.0));
        assertTrue(ex.getMessage().contains("engines.max"));
    }

    @Test
    void minValueOfRejectsValueBelowReferencedKey() {
        InMemoryConfigStorage storage = new InMemoryConfigStorage();
        Config config = Config.create("engines", storage);
        // engines.max must not fall below engines.min
        ConfigKey<Double> min = config.define("engines.min")
                .asDouble()
                .defaultValue(3.0)
                .min(0.0)
                .register();
        ConfigKey<Double> max = config.define("engines.max")
                .asDouble()
                .defaultValue(10.0)
                .minValueOf(() -> min)
                .register();
        config.load();

        assertDoesNotThrow(() -> config.set(max, 3.0), "equal to the bound is inclusive");
        assertDoesNotThrow(() -> config.set(max, 8.0));
        ConfigValidationException ex = assertThrows(ConfigValidationException.class, () -> config.set(max, 2.0));
        assertTrue(ex.getMessage().contains("engines.min"));
    }

    @Test
    void mutualCrossFieldValidatorsDoNotRecurse() {
        InMemoryConfigStorage storage = new InMemoryConfigStorage();
        Config config = Config.create("engines", storage);
        AtomicReference<ConfigKey<Double>> maxRef = new AtomicReference<>();
        ConfigKey<Double> min = config.define("engines.min")
                .asDouble()
                .defaultValue(3.0)
                .min(0.0)
                .maxValueOf(maxRef::get)
                .register();
        ConfigKey<Double> max = config.define("engines.max")
                .asDouble()
                .defaultValue(10.0)
                .min(0.0)
                .minValueOf(() -> min)
                .register();
        maxRef.set(max);

        assertDoesNotThrow(config::load, "mutual cross-field validators must not recurse");
        assertEquals(3.0, config.get(min));
        assertEquals(10.0, config.get(max));

        assertThrows(ConfigValidationException.class, () -> config.set(min, 20.0));
        assertThrows(ConfigValidationException.class, () -> config.set(max, 1.0));
    }
}

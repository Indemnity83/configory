package com.indemnity83.configory;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
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
        // stirling.min must not exceed stirling.max
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
}

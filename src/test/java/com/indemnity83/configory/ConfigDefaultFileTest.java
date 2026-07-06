package com.indemnity83.configory;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Proves a bare (dot-less) key flows through {@link Config} into the default {@code config}
 * file, and that bare and dotted keys can coexist in one config instance.
 */
class ConfigDefaultFileTest {

    @Test
    void bareKeyLandsInDefaultFile() {
        InMemoryConfigStorage storage = new InMemoryConfigStorage();
        Config config = Config.create("simplemod", storage);
        ConfigKey<Float> speed = config.define("speed_multiplier")
                .asFloat()
                .defaultValue(1.0f)
                .range(0.1f, 10.0f)
                .register();

        config.load();
        assertTrue(config.dirtyFiles().contains("config"), "bare key should target the 'config' file");
        assertEquals(1.0f, config.get(speed));

        config.save();
        assertTrue(storage.has("config"));
        assertFalse(storage.has("speed_multiplier"), "the key name must not become a file name");
    }

    @Test
    void bareKeyRoundTripsThroughSetAndGet() {
        InMemoryConfigStorage storage = new InMemoryConfigStorage();
        Config config = Config.create("simplemod", storage);
        ConfigKey<Integer> count =
                config.define("max_count").asInt().defaultValue(4).min(0).register();
        config.load();

        config.set(count, 9).save();
        assertEquals(9, config.get(count));
        assertEquals(9, config.get("max_count").asInt());
        // Explicit alias reaches the same value.
        assertEquals(9, config.get("config.max_count").asInt());
    }

    @Test
    void bareAndDottedKeysCoexistInSeparateFiles() {
        InMemoryConfigStorage storage = new InMemoryConfigStorage();
        Config config = Config.create("mixedmod", storage);
        config.define("volume").asDouble().defaultValue(0.5).register();
        config.define("engines.stirling.output").asDouble().defaultValue(3.0).register();

        config.load();
        config.save();

        assertTrue(storage.has("config"), "bare 'volume' lives in config.json");
        assertTrue(storage.has("engines"), "dotted key lives in engines.json");
    }
}

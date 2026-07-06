package com.indemnity83.configory;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConfigValueTest {

    private Config config;

    @BeforeEach
    void setUp() {
        config = Config.create("host", new InMemoryConfigStorage());
        config.define("core.speed").asDouble().defaultValue(2.5).register();
        config.define("core.name").asString().defaultValue("engine").register();
        config.load();
    }

    @Test
    void readsTypedValueByPath() {
        assertEquals(2.5, config.get("core.speed").asDouble());
        assertEquals("engine", config.get("core.name").asString());
    }

    @Test
    void strictReadThrowsForMissingValue() {
        ConfigValue value = config.get("misc.unknown");
        ConfigException ex = assertThrows(ConfigException.class, value::asInt);
        assertTrue(ex.getMessage().contains("misc.unknown"));
    }

    @Test
    void fallbackReadReturnsFallbackForMissingValue() {
        ConfigValue value = config.get("misc.unknown");
        assertEquals(42, value.asInt(42));
        assertEquals("x", value.asString("x"));
        assertTrue(value.asBoolean(true));
        assertEquals(3L, value.asLong(3L));
        assertEquals(1.5f, value.asFloat(1.5f));
        assertEquals(9.0, value.asDouble(9.0));
    }

    @Test
    void fallbackReadReturnsFallbackForWrongType() {
        // core.name holds a string; asDouble with a fallback must not throw.
        assertEquals(0.0, config.get("core.name").asDouble(0.0));
    }

    @Test
    void strictReadThrowsForWrongType() {
        assertThrows(ConfigException.class, () -> config.get("core.name").asDouble());
    }

    @Test
    void isPresentReportsStoredValues() {
        ConfigValue value = config.get("core.speed");
        assertTrue(value.isPresent());
        assertFalse(value.isEmpty());
    }

    @Test
    void isPresentReportsMissingValues() {
        ConfigValue value = config.get("misc.unknown");
        assertFalse(value.isPresent());
        assertTrue(value.isEmpty());
    }

    @Test
    void isPresentIgnoresType() {
        assertTrue(config.get("core.name").isPresent());
    }
}

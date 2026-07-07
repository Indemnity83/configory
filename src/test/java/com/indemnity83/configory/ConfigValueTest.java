package com.indemnity83.configory;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
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

    @Test
    void isPresentIsFalseForExplicitJsonNull() {
        InMemoryConfigStorage storage = new InMemoryConfigStorage();
        JsonObject document = new JsonObject();
        JsonPaths.set(document, ConfigPath.parse("core.blank"), JsonNull.INSTANCE);
        storage.seed("core", document);
        Config withNull = Config.create("host", storage);

        assertFalse(withNull.get("core.blank").isPresent());
        assertTrue(withNull.get("core.blank").isEmpty());
    }

    @Test
    void strictReadsBooleanAndLong() {
        Config typed = Config.create("host", new InMemoryConfigStorage());
        typed.define("core.enabled").asBoolean().defaultValue(true).register();
        typed.define("core.count").asLong().defaultValue(42L).register();
        typed.load();

        assertTrue(typed.get("core.enabled").asBoolean());
        assertEquals(42L, typed.get("core.count").asLong());
    }

    @Test
    void fallbackReadReturnsStoredValueWhenPresent() {
        assertEquals(2.5, config.get("core.speed").asDouble(9.9));
        assertEquals("engine", config.get("core.name").asString("other"));
    }

    @Test
    void asDisplayStringRendersAnyTypeWithoutSwitching() {
        Config typed = Config.create("disp", new InMemoryConfigStorage());
        typed.define("flag").asBoolean().defaultValue(true).register();
        typed.define("count").asInt().defaultValue(42).register();
        typed.define("name").asString().defaultValue("x").register();
        typed.load();

        assertEquals("true", typed.get("flag").asDisplayString());
        assertEquals("42", typed.get("count").asDisplayString());
        assertEquals("x", typed.get("name").asDisplayString());
    }

    @Test
    void asDisplayStringIsEmptyWhenAbsent() {
        assertEquals("", config.get("misc.unknown").asDisplayString());
    }
}

package com.indemnity83.configory;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConfigDefineShorthandTest {

    private Config config;

    @BeforeEach
    void setUp() {
        config = Config.create("logistics", new InMemoryConfigStorage());
    }

    @Test
    void shorthandSelectsTheSameTypeAsTheLongForm() {
        // defineX(path) should be equivalent to define(path).asX()
        assertEquals(
                ConfigType.BOOLEAN,
                config.defineBoolean("a.bool")
                        .defaultValue(true)
                        .register()
                        .definition()
                        .type());
        assertEquals(
                ConfigType.STRING,
                config.defineString("a.str")
                        .defaultValue("x")
                        .register()
                        .definition()
                        .type());
        assertEquals(
                ConfigType.INT,
                config.defineInt("a.int")
                        .defaultValue(1)
                        .register()
                        .definition()
                        .type());
        assertEquals(
                ConfigType.LONG,
                config.defineLong("a.long")
                        .defaultValue(1L)
                        .register()
                        .definition()
                        .type());
        assertEquals(
                ConfigType.FLOAT,
                config.defineFloat("a.float")
                        .defaultValue(1.0f)
                        .register()
                        .definition()
                        .type());
        assertEquals(
                ConfigType.DOUBLE,
                config.defineDouble("a.double")
                        .defaultValue(1.0)
                        .register()
                        .definition()
                        .type());
    }

    @Test
    void defaultParameterPresetsTheDefaultValue() {
        // The two-arg form registers without a separate defaultValue() call.
        ConfigKey<Boolean> bool = config.defineBoolean("core.bool", true).register();
        ConfigKey<String> str = config.defineString("core.str", "auto").register();
        ConfigKey<Integer> intKey = config.defineInt("core.int", 64).register();
        ConfigKey<Long> longKey = config.defineLong("core.long", 100L).register();
        ConfigKey<Float> floatKey = config.defineFloat("core.float", 1.5f).register();
        ConfigKey<Double> doubleKey = config.defineDouble("core.double", 0.85).register();

        assertEquals(true, config.get(bool));
        assertEquals("auto", config.get(str));
        assertEquals(64, config.get(intKey));
        assertEquals(100L, config.get(longKey));
        assertEquals(1.5f, config.get(floatKey));
        assertEquals(0.85, config.get(doubleKey));
    }

    @Test
    void constraintsStillChainAfterThePresetDefault() {
        ConfigKey<Integer> maxArea =
                config.defineInt("machines.quarry.max_area", 64).range(1, 256).register();
        config.load();

        assertDoesNotThrow(() -> config.set(maxArea, 128));
        assertThrows(ConfigValidationException.class, () -> config.set(maxArea, 999));
    }

    @Test
    void stringConstraintsStillChainAfterThePresetDefault() {
        ConfigKey<String> unit = config.defineString("core.energy_unit", "FE")
                .allowedValues("FE", "RF", "MJ")
                .register();
        config.load();

        assertDoesNotThrow(() -> config.set(unit, "RF"));
        assertThrows(ConfigValidationException.class, () -> config.set(unit, "banana"));
    }

    @Test
    void shorthandWithoutADefaultStillRequiresOneToRegister() {
        ConfigException ex = assertThrows(ConfigException.class, () -> config.defineFloat("core.no_default")
                .register());
        assertTrue(ex.getMessage().contains("missing a default"));
    }

    @Test
    void presetDefaultIsWrittenToDiskOnLoad() {
        ConfigKey<Double> efficiency =
                config.defineDouble("machines.efficiency", 0.85).register();
        config.load();
        assertTrue(config.isDirty());
        assertEquals(0.85, config.get(efficiency));
    }
}

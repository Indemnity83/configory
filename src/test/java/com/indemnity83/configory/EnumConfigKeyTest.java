package com.indemnity83.configory;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EnumConfigKeyTest {

    enum Mode {
        OFF,
        ON,
        AUTO
    }

    private Config newConfig() {
        return Config.create("enums", new InMemoryConfigStorage());
    }

    @Test
    void roundTripsEnumStoredByName() {
        Config config = newConfig();
        ConfigKey<Mode> mode = config.defineEnum("core.mode", Mode.OFF).register();
        config.load();

        assertEquals(Mode.OFF, config.get(mode));

        config.set(mode, Mode.AUTO).save();
        assertEquals(Mode.AUTO, config.get(mode));
        assertEquals("AUTO", config.get("core.mode").asString(), "stored by constant name");
    }

    @Test
    void unknownStoredNameIsRepairedToDefaultOnLoad() {
        Config config = newConfig();
        ConfigKey<Mode> mode = config.defineEnum("core.mode", Mode.ON).register();

        config.set("core.mode", "SIDEWAYS").save();
        config.discardAndReload();

        assertEquals(Mode.ON, config.get(mode), "unknown constant repaired to default");
        assertTrue(config.isDirty());
    }

    @Test
    void asEnumReadsDynamicallyWithAndWithoutFallback() {
        Config config = newConfig();
        config.defineEnum("core.mode", Mode.AUTO).register();
        config.load();

        assertEquals(Mode.AUTO, config.get("core.mode").asEnum(Mode.class));
        assertEquals(Mode.ON, config.get("misc.absent").asEnum(Mode.class, Mode.ON));
    }

    @Test
    void classOverloadDefinesWithASeparateDefault() {
        Config config = newConfig();
        ConfigKey<Mode> mode =
                config.defineEnum("core.mode", Mode.class).defaultValue(Mode.ON).register();
        config.load();

        assertEquals(Mode.ON, config.get(mode));
    }
}

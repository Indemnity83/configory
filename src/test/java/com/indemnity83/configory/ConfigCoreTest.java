package com.indemnity83.configory;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConfigCoreTest {

    private InMemoryConfigStorage storage;
    private Config config;
    private ConfigKey<Float> speed;

    @BeforeEach
    void setUp() {
        storage = new InMemoryConfigStorage();
        config = Config.create("logistics", storage);
        speed = config.define("core.speed_multiplier")
                .asFloat()
                .defaultValue(1.0f)
                .range(0.1f, 10.0f)
                .describe("Global speed multiplier.")
                .register();
    }

    private JsonObject documentWith(String path, com.google.gson.JsonElement value) {
        JsonObject doc = new JsonObject();
        JsonPaths.set(doc, ConfigPath.parse(path), value);
        return doc;
    }

    @Test
    void loadWritesDefaultsAndMarksDirty() {
        config.load();
        assertTrue(config.isDirty(), "missing keys get their defaults written, which is a pending change");
        assertTrue(config.dirtyFiles().contains("core"));
        assertEquals(1.0f, config.get(speed));
    }

    @Test
    void saveClearsDirtyAndPersists() {
        config.load();
        config.save();
        assertFalse(config.isDirty());
        assertTrue(storage.has("core"));
    }

    @Test
    void getReturnsStoredValueOverDefault() {
        storage.seed("core", documentWith("core.speed_multiplier", new JsonPrimitive(3.0f)));
        config.load();
        assertEquals(3.0f, config.get(speed));
    }

    @Test
    void getFallsBackToDefaultWhenAbsentBeforeLoad() {
        // No load() call: document is lazily empty, so the key resolves to its default.
        assertEquals(1.0f, config.get(speed));
    }

    @Test
    void setByKeyRejectsOutOfRangeValue() {
        config.load();
        ConfigValidationException ex = assertThrows(ConfigValidationException.class, () -> config.set(speed, 20.0f));
        assertTrue(ex.getMessage().contains("core.speed_multiplier"));
        assertTrue(ex.getMessage().contains("at most"));
    }

    @Test
    void setByKeyAcceptsValidValueAndTracksDirty() {
        config.load();
        config.save();
        assertFalse(config.isDirty());

        ConfigMutation mutation = config.set(speed, 5.0f);
        assertTrue(mutation.changed());
        assertTrue(config.isDirty());
        assertEquals(5.0f, config.get(speed));

        mutation.save();
        assertFalse(config.isDirty());
    }

    @Test
    void setByKeyToSameValueIsNotAChange() {
        config.load();
        config.save();
        ConfigMutation mutation = config.set(speed, 1.0f);
        assertFalse(mutation.changed());
        assertFalse(config.isDirty());
    }

    @Test
    void setByStringPathSkipsDefinitionValidation() {
        config.load();
        // A raw string set is untyped and unvalidated: it accepts an out-of-range value...
        config.set("core.speed_multiplier", 99.0f);
        assertEquals(99.0f, config.get("core.speed_multiplier").asFloat());
        // ...but reading through the typed key re-validates and rejects it.
        assertThrows(ConfigValidationException.class, () -> config.get(speed));
    }

    @Test
    void trySetByStringPathAcceptsValidValue() {
        config.load();
        assertTrue(config.trySet("core.speed_multiplier", 5.0f));
        assertEquals(5.0f, config.get(speed));
    }

    @Test
    void trySetByStringPathRejectsOutOfRangeValueAndWritesNothing() {
        config.load();
        config.save();
        assertFalse(config.trySet("core.speed_multiplier", 99.0f));
        assertEquals(1.0f, config.get(speed));
        assertFalse(config.isDirty());
    }

    @Test
    void trySetByStringPathRejectsWrongType() {
        config.load();
        assertFalse(config.trySet("core.speed_multiplier", "fast"));
        assertEquals(1.0f, config.get(speed));
    }

    @Test
    void trySetByStringPathWritesWhereNoDefinitionExists() {
        assertTrue(config.trySet("misc.freeform", 42));
        assertEquals(42, config.get("misc.freeform").asInt());
    }

    @Test
    void trySetByKeyRejectsOutOfRangeValue() {
        config.load();
        assertFalse(config.trySet(speed, 20.0f));
        assertEquals(1.0f, config.get(speed));
    }

    @Test
    void trySetByKeyAcceptsValidValue() {
        config.load();
        assertTrue(config.trySet(speed, 5.0f));
        assertEquals(5.0f, config.get(speed));
    }

    @Test
    void trySetByKeyRejectsForeignKey() {
        ConfigKey<Float> foreign = Config.create("other", new InMemoryConfigStorage())
                .define("core.speed_multiplier")
                .asFloat()
                .defaultValue(1.0f)
                .register();
        assertThrows(ConfigException.class, () -> config.trySet(foreign, 5.0f));
    }

    @Test
    void invalidStoredValueIsRepairedToDefaultOnLoad() {
        storage.seed("core", documentWith("core.speed_multiplier", new JsonPrimitive(99.0f)));
        config.load();
        assertEquals(1.0f, config.get(speed), "out-of-range stored value should reset to default");
        assertTrue(config.dirtyFiles().contains("core"));
    }

    @Test
    void wrongTypeStoredValueIsRepairedToDefaultOnLoad() {
        storage.seed("core", documentWith("core.speed_multiplier", new JsonPrimitive("not-a-number")));
        config.load();
        assertEquals(1.0f, config.get(speed));
    }

    @Test
    void reloadRefusesWhenDirty() {
        config.load();
        config.set(speed, 5.0f);
        ConfigException ex = assertThrows(ConfigException.class, () -> config.reload());
        assertTrue(ex.getMessage().contains("unsaved changes"));
    }

    @Test
    void discardAndReloadDropsUnsavedChanges() {
        config.load();
        config.save();
        config.set(speed, 5.0f);
        config.discardAndReload();
        assertEquals(1.0f, config.get(speed));
        assertFalse(config.isDirty());
    }

    @Test
    void reloadSucceedsAfterSave() {
        config.load();
        config.set(speed, 5.0f);
        config.save();
        assertDoesNotThrow(() -> config.reload());
        assertEquals(5.0f, config.get(speed));
    }

    @Test
    void duplicateDefinitionIsRejected() {
        ConfigException ex = assertThrows(ConfigException.class, () -> config.define("core.speed_multiplier")
                .asFloat()
                .defaultValue(2.0f)
                .register());
        assertTrue(ex.getMessage().contains("Duplicate"));
    }

    @Test
    void registerWithoutDefaultIsRejected() {
        ConfigException ex = assertThrows(
                ConfigException.class, () -> config.define("core.other").asInt().register());
        assertTrue(ex.getMessage().contains("missing a default"));
    }

    @Test
    void keyFromAnotherConfigIsRejected() {
        Config other = Config.create("other", new InMemoryConfigStorage());
        ConfigKey<Integer> foreign = other.define("a.b").asInt().defaultValue(1).register();
        ConfigException ex = assertThrows(ConfigException.class, () -> config.get(foreign));
        assertTrue(ex.getMessage().contains("does not belong"));
    }

    @Test
    void customValidatorIsApplied() {
        ConfigKey<Integer> even = config.define("core.even")
                .asInt()
                .defaultValue(2)
                .validator(
                        (value, cfg) -> value % 2 == 0 ? ValidationResult.ok() : ValidationResult.error("must be even"))
                .register();
        config.load();
        assertThrows(ConfigValidationException.class, () -> config.set(even, 3));
        assertDoesNotThrow(() -> config.set(even, 4));
    }

    @Test
    void allowedValuesConstraintForStrings() {
        ConfigKey<String> mode = config.define("core.mode")
                .asString()
                .defaultValue("auto")
                .allowedValues("auto", "manual")
                .register();
        config.load();
        assertThrows(ConfigValidationException.class, () -> config.set(mode, "banana"));
        assertDoesNotThrow(() -> config.set(mode, "manual"));
    }

    @Test
    void definitionsAreExposed() {
        assertEquals(1, config.definitions().size());
        assertEquals(
                "core.speed_multiplier",
                config.definitions().iterator().next().path().fullPath());
    }
}

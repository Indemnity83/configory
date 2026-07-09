package com.indemnity83.configory;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConfigRenameMigrationTest {

    private InMemoryConfigStorage storage;
    private Config config;

    @BeforeEach
    void setUp() {
        storage = new InMemoryConfigStorage();
        config = Config.create("logistics", storage);
    }

    private void seed(String path, com.google.gson.JsonElement value) {
        JsonObject document = new JsonObject();
        JsonPaths.set(document, ConfigPath.parse(path), value);
        storage.seed("logistics", document);
    }

    @Test
    void adoptsAFormerValueWhenThePrimaryIsAbsent() {
        seed("core.old_speed", new JsonPrimitive(3.0f));
        ConfigKey<Float> speed = config.defineFloat("core.speed_multiplier", 1.0f)
                .formerly("core.old_speed")
                .register();

        config.load();

        assertEquals(3.0f, config.get(speed));
        assertTrue(config.isDirty());
    }

    @Test
    void stripsTheFormerPathFromTheDocumentAfterMigrating() {
        seed("core.old_speed", new JsonPrimitive(3.0f));
        config.defineFloat("core.speed_multiplier", 1.0f)
                .formerly("core.old_speed")
                .register();

        config.load();

        assertTrue(config.get("core.old_speed").isEmpty());
        assertEquals(3.0f, config.get("core.speed_multiplier").asFloat());
    }

    @Test
    void stripsAStaleFormerPathEvenWhenThePrimaryAlreadyHasAValue() {
        JsonObject document = new JsonObject();
        JsonPaths.set(document, ConfigPath.parse("core.speed_multiplier"), new JsonPrimitive(2.0f));
        JsonPaths.set(document, ConfigPath.parse("core.old_speed"), new JsonPrimitive(9.0f));
        storage.seed("logistics", document);

        ConfigKey<Float> speed = config.defineFloat("core.speed_multiplier", 1.0f)
                .formerly("core.old_speed")
                .register();

        config.load();

        assertEquals(2.0f, config.get(speed));
        assertTrue(config.get("core.old_speed").isEmpty());
    }

    @Test
    void searchesFormerPathsInDeclarationOrder() {
        JsonObject document = new JsonObject();
        JsonPaths.set(document, ConfigPath.parse("legacy.speed"), new JsonPrimitive(5.0f));
        JsonPaths.set(document, ConfigPath.parse("core.old_speed"), new JsonPrimitive(3.0f));
        storage.seed("logistics", document);

        ConfigKey<Float> speed = config.defineFloat("core.speed_multiplier", 1.0f)
                .formerly("core.old_speed")
                .formerly("legacy.speed")
                .register();

        config.load();

        assertEquals(3.0f, config.get(speed));
    }

    @Test
    void fallsThroughToTheNextFormerPathWhenAValueFailsValidation() {
        JsonObject document = new JsonObject();
        JsonPaths.set(document, ConfigPath.parse("core.old_speed"), new JsonPrimitive(999.0f));
        JsonPaths.set(document, ConfigPath.parse("legacy.speed"), new JsonPrimitive(4.0f));
        storage.seed("logistics", document);

        ConfigKey<Float> speed = config.defineFloat("core.speed_multiplier", 1.0f)
                .range(0.1f, 10.0f)
                .formerly("core.old_speed")
                .formerly("legacy.speed")
                .register();

        config.load();

        assertEquals(4.0f, config.get(speed));
    }

    @Test
    void fallsBackToTheDefaultWhenNoFormerPathHasAUsableValue() {
        seed("core.old_speed", new JsonPrimitive("not-a-number"));
        ConfigKey<Float> speed = config.defineFloat("core.speed_multiplier", 1.0f)
                .formerly("core.old_speed")
                .register();

        config.load();

        assertEquals(1.0f, config.get(speed));
    }

    @Test
    void theRenameSettlesOnDiskAfterSave() {
        seed("core.old_speed", new JsonPrimitive(3.0f));
        config.defineFloat("core.speed_multiplier", 1.0f)
                .formerly("core.old_speed")
                .register();

        config.load().save();

        JsonObject persisted = storage.raw("logistics");
        assertEquals(
                3.0f,
                JsonPaths.get(persisted, ConfigPath.parse("core.speed_multiplier"))
                        .getAsFloat());
        assertNull(JsonPaths.get(persisted, ConfigPath.parse("core.old_speed")));
    }

    @Test
    void aFormerPathThatClashesWithARegisteredKeyFailsFast() {
        config.defineFloat("core.speed", 1.0f).register();

        ConfigException ex = assertThrows(ConfigException.class, () -> config.defineFloat("core.speed_multiplier", 1.0f)
                .formerly("core.speed")
                .register());
        assertTrue(ex.getMessage().contains("core.speed"));
    }

    @Test
    void aLaterKeyWhosePathIsAnExistingFormerPathFailsFast() {
        config.defineFloat("core.speed_multiplier", 1.0f)
                .formerly("core.old_speed")
                .register();

        ConfigException ex = assertThrows(ConfigException.class, () -> config.defineFloat("core.old_speed", 1.0f)
                .register());
        assertTrue(ex.getMessage().contains("former path"));
    }

    @Test
    void twoKeysSharingAFormerPathFailFast() {
        config.defineFloat("core.speed_multiplier", 1.0f)
                .formerly("core.old_speed")
                .register();

        ConfigException ex = assertThrows(ConfigException.class, () -> config.defineFloat("core.velocity", 1.0f)
                .formerly("core.old_speed")
                .register());
        assertTrue(ex.getMessage().contains("former path"));
    }

    @Test
    void aFormerPathThatOverlapsItsOwnKeyFailsFast() {
        ConfigException ex = assertThrows(
                ConfigException.class,
                () -> config.defineFloat("core.speed", 1.0f).formerly("core").register());
        assertTrue(ex.getMessage().contains("overlaps"));
    }

    @Test
    void aFormerPathThatIsAnAncestorOfARegisteredKeyFailsFast() {
        config.defineFloat("core.other_key", 1.0f).register();

        ConfigException ex = assertThrows(
                ConfigException.class,
                () -> config.defineFloat("engines.speed", 1.0f).formerly("core").register());
        assertTrue(ex.getMessage().contains("overlaps"));
    }

    @Test
    void aLaterKeyNestedUnderAnExistingFormerPathFailsFast() {
        config.defineFloat("engines.speed", 1.0f).formerly("core").register();

        ConfigException ex = assertThrows(ConfigException.class, () -> config.defineFloat("core.other_key", 1.0f)
                .register());
        assertTrue(ex.getMessage().contains("overlaps"));
    }
}

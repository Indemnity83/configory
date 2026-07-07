package com.indemnity83.configory;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Exercises the full consuming-mod flow through the registry and default file storage.
 * The main config persists to {@code ./config/<modId>.json} and child configs to
 * {@code ./config/<modId>/*.json}, so this test cleans up both afterwards.
 */
class ConfigBootstrapIntegrationTest {

    private final Path mainFile = Path.of("config").resolve(BootstrapTestMod.MOD_ID + ".json");
    private final Path childDir = Path.of("config").resolve(BootstrapTestMod.MOD_ID);

    @AfterEach
    void cleanUp() throws IOException {
        Files.deleteIfExists(mainFile);
        if (Files.exists(childDir)) {
            try (var paths = Files.walk(childDir)) {
                paths.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

    @Test
    void bootstrapInitializesRegistersLoadsAndPersists() throws IOException {
        BootstrapTestMod mod = new BootstrapTestMod();
        mod.bootstrapConfig(BootstrapTestMod.MOD_ID);

        assertTrue(BootstrapTestMod.Configs.bootstrapRan, "optional bootstrap hook should run");

        // Defaults resolve through the typed keys and the raw path accessor.
        assertEquals(1.0f, mod.getConfig(BootstrapTestMod.Configs.SPEED));
        assertEquals(4, (int) mod.getConfig(BootstrapTestMod.Configs.COUNT));
        assertEquals(1.0f, mod.getConfig("core.speed").asFloat());

        // The child config was auto-loaded by bootstrap (its defaults were applied, marking it dirty).
        Config engines = mod.config(BootstrapTestMod.MOD_ID + ".engines");
        assertTrue(engines.isDirty(), "bootstrap should have loaded the child config");
        assertEquals(3.0, engines.get(BootstrapTestMod.Configs.STIRLING_MIN));

        // Save persists the main file and the child under the mod's folder.
        mod.setConfig(BootstrapTestMod.Configs.SPEED, 3.0f).save();
        engines.save();
        assertTrue(Files.exists(mainFile));
        assertTrue(Files.exists(childDir.resolve("engines.json")));

        // A reload reads the persisted value back.
        mod.reloadConfig();
        assertEquals(3.0f, mod.getConfig(BootstrapTestMod.Configs.SPEED));
    }

    @Test
    void noArgBootstrapResolvesModIdFromHost() {
        BootstrapTestMod mod = new BootstrapTestMod();
        assertDoesNotThrow(() -> mod.bootstrapConfig());
        assertEquals(4, (int) mod.getConfig(BootstrapTestMod.Configs.COUNT));
    }
}

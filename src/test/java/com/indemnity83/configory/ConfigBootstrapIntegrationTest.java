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

        // A fresh install gets editable files with no manual save: bootstrap persisted the applied
        // defaults for the main config and the auto-loaded child.
        assertTrue(Files.exists(mainFile), "bootstrap should write the main config file");
        assertTrue(Files.exists(childDir.resolve("engines.json")), "bootstrap should write the child config file");
        assertFalse(mod.config(BootstrapTestMod.MOD_ID).isDirty(), "persisted defaults leave nothing pending");

        Config engines = mod.config(BootstrapTestMod.MOD_ID + ".engines");
        assertFalse(engines.isDirty());
        assertEquals(3.0, engines.get(BootstrapTestMod.Configs.STIRLING_MIN));

        // Mutate + save + reload round-trips the value on disk.
        mod.setConfig(BootstrapTestMod.Configs.SPEED, 3.0f).save();
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

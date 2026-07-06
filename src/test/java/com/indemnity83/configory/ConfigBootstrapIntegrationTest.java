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
 * The registry resolves config files under {@code ./config/<modId>/}, so this test cleans
 * up that directory afterwards.
 */
class ConfigBootstrapIntegrationTest {

    private final Path configDir = Path.of("config").resolve(BootstrapTestMod.MOD_ID);

    @AfterEach
    void cleanUp() throws IOException {
        if (Files.exists(configDir)) {
            try (var paths = Files.walk(configDir)) {
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

        // Mutate + save persists a real file.
        mod.setConfig(BootstrapTestMod.Configs.SPEED, 3.0f).save();
        assertTrue(Files.exists(configDir.resolve("core.json")));

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

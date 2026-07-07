package com.indemnity83.configory;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ConfigRegistryTest {

    @Test
    void getOrCreateReturnsTheSameInstancePerId() {
        Config first = ConfigRegistry.getOrCreate("reg-same");
        Config second = ConfigRegistry.getOrCreate("reg-same");
        assertSame(first, second);
        assertEquals("reg-same", first.id());
    }

    @Test
    void distinctIdsGetDistinctConfigs() {
        assertNotSame(ConfigRegistry.getOrCreate("reg-a"), ConfigRegistry.getOrCreate("reg-b"));
    }

    @Test
    void getOrCreateWithStorageUsesTheInjectedStorage() {
        InMemoryConfigStorage storage = new InMemoryConfigStorage();
        Config config = ConfigRegistry.getOrCreate("reg-inject", storage);
        config.define("core.value").asInt().defaultValue(7).register();
        config.load();
        config.save();
        assertTrue(storage.has("core"), "defaults were written to the injected storage, not the file store");
    }

    @Test
    void storageIsIgnoredWhenTheConfigAlreadyExists() {
        Config first = ConfigRegistry.getOrCreate("reg-existing");
        Config second = ConfigRegistry.getOrCreate("reg-existing", new InMemoryConfigStorage());
        assertSame(first, second);
    }

    @Test
    void configForWithStorageRoutesThroughTheRegistry() {
        InMemoryConfigStorage storage = new InMemoryConfigStorage();
        Config viaEntries = ConfigEntries.configFor("reg-entries", storage);
        assertSame(viaEntries, ConfigRegistry.getOrCreate("reg-entries"));
    }
}

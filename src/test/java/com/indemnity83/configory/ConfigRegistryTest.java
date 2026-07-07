package com.indemnity83.configory;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
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
        assertTrue(storage.has("reg-inject"), "defaults were written to the injected storage, not the file store");
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

    @Test
    void childConfigsReturnsOnlyNestedIds() {
        Config modA = ConfigRegistry.getOrCreate("cc-modA");
        Config modASub = ConfigRegistry.getOrCreate("cc-modA.sub");
        Config modB = ConfigRegistry.getOrCreate("cc-modB");
        Config modBSub = ConfigRegistry.getOrCreate("cc-modB.sub");

        List<Config> children = ConfigRegistry.childConfigs("cc-modA");

        assertTrue(children.contains(modASub));
        assertFalse(children.contains(modA), "the mod itself is not its own child");
        assertFalse(children.contains(modB));
        assertFalse(children.contains(modBSub));
    }

    @Test
    void childConfigsRespectsTheTrailingDotBoundary() {
        Config logi = ConfigRegistry.getOrCreate("cc-logi");
        Config logistics = ConfigRegistry.getOrCreate("cc-logistics");

        List<Config> children = ConfigRegistry.childConfigs("cc-logi");

        assertFalse(children.contains(logistics), "\"cc-logi\" must not match \"cc-logistics\"");
        assertFalse(children.contains(logi));
    }
}

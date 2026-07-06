package com.indemnity83.configory;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class ConfigPathTest {

    @Test
    void parsesFileAndSegments() {
        ConfigPath path = ConfigPath.parse("engines.stirling.max_output");
        assertEquals("engines", path.file());
        assertEquals(List.of("stirling", "max_output"), path.segments());
        assertEquals("engines.stirling.max_output", path.fullPath());
        assertEquals("engines.stirling.max_output", path.toString());
    }

    @Test
    void parsesMinimalTwoSegmentPath() {
        ConfigPath path = ConfigPath.parse("core.speed");
        assertEquals("core", path.file());
        assertEquals(List.of("speed"), path.segments());
    }

    @Test
    void bareKeyUsesDefaultFile() {
        ConfigPath path = ConfigPath.parse("speed_multiplier");
        assertEquals(ConfigPath.DEFAULT_FILE, path.file());
        assertEquals("config", path.file());
        assertEquals(List.of("speed_multiplier"), path.segments());
        // Bare keys still render in qualified form.
        assertEquals("config.speed_multiplier", path.fullPath());
    }

    @Test
    void explicitConfigPrefixEqualsBareKey() {
        assertEquals(ConfigPath.parse("config.speed"), ConfigPath.parse("speed"));
    }

    @Test
    void nestedKeysUnderDefaultFile() {
        ConfigPath path = ConfigPath.parse("config.section.key");
        assertEquals("config", path.file());
        assertEquals(List.of("section", "key"), path.segments());
    }

    @Test
    void rejectsEmptySegment() {
        assertThrows(ConfigException.class, () -> ConfigPath.parse("core..speed"));
    }

    @Test
    void rejectsTrailingDot() {
        assertThrows(ConfigException.class, () -> ConfigPath.parse("foo."));
    }

    @Test
    void rejectsLeadingDot() {
        assertThrows(ConfigException.class, () -> ConfigPath.parse(".foo"));
    }

    @Test
    void rejectsNullRaw() {
        assertThrows(NullPointerException.class, () -> ConfigPath.parse(null));
    }

    @Test
    void constructorRejectsBlankFile() {
        assertThrows(ConfigException.class, () -> new ConfigPath("  ", List.of("key")));
    }

    @Test
    void constructorRejectsEmptySegments() {
        assertThrows(ConfigException.class, () -> new ConfigPath("file", List.of()));
    }

    @Test
    void segmentsAreImmutable() {
        ConfigPath path = ConfigPath.parse("core.speed");
        assertThrows(UnsupportedOperationException.class, () -> path.segments().add("x"));
    }

    @Test
    void equalPathsAreEqual() {
        assertEquals(ConfigPath.parse("a.b.c"), ConfigPath.parse("a.b.c"));
        assertEquals(
                ConfigPath.parse("a.b.c").hashCode(), ConfigPath.parse("a.b.c").hashCode());
    }
}

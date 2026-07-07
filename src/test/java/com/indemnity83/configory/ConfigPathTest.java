package com.indemnity83.configory;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConfigPathTest {

    @Test
    void everyDotIsANestingBoundary() {
        ConfigPath path = ConfigPath.parse("engines.stirling.max_output");
        assertEquals(List.of("engines", "stirling", "max_output"), path.segments());
        assertEquals("engines.stirling.max_output", path.fullPath());
        assertEquals("engines.stirling.max_output", path.toString());
    }

    @Test
    void parsesTwoSegmentPath() {
        ConfigPath path = ConfigPath.parse("core.speed");
        assertEquals(List.of("core", "speed"), path.segments());
    }

    @Test
    void dotlessPathIsASingleTopLevelKey() {
        ConfigPath path = ConfigPath.parse("speed_multiplier");
        assertEquals(List.of("speed_multiplier"), path.segments());
        assertEquals("speed_multiplier", path.fullPath());
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
    void constructorRejectsEmptySegments() {
        assertThrows(ConfigException.class, () -> new ConfigPath(List.of()));
    }

    @Test
    void constructorRejectsBlankSegment() {
        assertThrows(ConfigException.class, () -> new ConfigPath(Arrays.asList("ok", "  ")));
    }

    @Test
    void constructorRejectsNullSegment() {
        assertThrows(ConfigException.class, () -> new ConfigPath(Arrays.asList("ok", null)));
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

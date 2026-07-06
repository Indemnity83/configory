package com.indemnity83.configory;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ConfigHostSupportTest {

    static final class WithModId {
        public static final String MOD_ID = "abc";
    }

    static final class WithLegacyModid {
        public static final String MODID = "xyz";
    }

    static final class WithBlankModId {
        public static final String MOD_ID = "   ";
    }

    static final class WithNonStaticModId {
        public final String MOD_ID = "instance";
    }

    static final class WithNothing {}

    @Test
    void resolvesModIdField() {
        assertEquals("abc", ConfigHostSupport.resolveModId(WithModId.class));
    }

    @Test
    void fallsBackToLegacyModidField() {
        assertEquals("xyz", ConfigHostSupport.resolveModId(WithLegacyModid.class));
    }

    @Test
    void blankModIdIsTreatedAsMissing() {
        assertThrows(ConfigException.class, () -> ConfigHostSupport.resolveModId(WithBlankModId.class));
    }

    @Test
    void nonStaticFieldIsIgnored() {
        assertThrows(ConfigException.class, () -> ConfigHostSupport.resolveModId(WithNonStaticModId.class));
    }

    @Test
    void missingFieldThrowsHelpfully() {
        ConfigException ex =
                assertThrows(ConfigException.class, () -> ConfigHostSupport.resolveModId(WithNothing.class));
        assertTrue(ex.getMessage().contains("Unable to resolve config id"));
    }
}

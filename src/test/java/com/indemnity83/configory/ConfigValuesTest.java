package com.indemnity83.configory;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;

class ConfigValuesTest {

    @Test
    void roundTripsEachSupportedType() {
        assertEquals(true, ConfigValues.fromJson(ConfigValues.toJson(true), ConfigType.BOOLEAN));
        assertEquals("hi", ConfigValues.fromJson(ConfigValues.toJson("hi"), ConfigType.STRING));
        assertEquals(7, (int) ConfigValues.<Integer>fromJson(ConfigValues.toJson(7), ConfigType.INT));
        assertEquals(9L, (long) ConfigValues.<Long>fromJson(ConfigValues.toJson(9L), ConfigType.LONG));
        assertEquals(1.5f, ConfigValues.<Float>fromJson(ConfigValues.toJson(1.5f), ConfigType.FLOAT));
        assertEquals(2.5d, ConfigValues.<Double>fromJson(ConfigValues.toJson(2.5d), ConfigType.DOUBLE));
    }

    @Test
    void rejectsUnsupportedType() {
        ConfigException ex = assertThrows(ConfigException.class, () -> ConfigValues.toJson(new Object()));
        assertTrue(ex.getMessage().contains("Unsupported"));
    }

    @Test
    void rejectsMissingAndNullElements() {
        assertThrows(ConfigException.class, () -> ConfigValues.fromJson(null, ConfigType.INT));
        assertThrows(ConfigException.class, () -> ConfigValues.fromJson(JsonNull.INSTANCE, ConfigType.INT));
    }

    @Test
    void rejectsNonPrimitive() {
        assertThrows(ConfigException.class, () -> ConfigValues.fromJson(new JsonObject(), ConfigType.INT));
    }

    @Test
    void rejectsTypeMismatch() {
        assertThrows(ConfigException.class, () -> ConfigValues.fromJson(new JsonPrimitive("nope"), ConfigType.BOOLEAN));
        assertThrows(ConfigException.class, () -> ConfigValues.fromJson(new JsonPrimitive(1), ConfigType.STRING));
        assertThrows(ConfigException.class, () -> ConfigValues.fromJson(new JsonPrimitive(true), ConfigType.INT));
    }

    @Test
    void rejectsFractionalWholeNumberTypes() {
        assertThrows(ConfigException.class, () -> ConfigValues.fromJson(new JsonPrimitive(1.5), ConfigType.INT));
        assertThrows(ConfigException.class, () -> ConfigValues.fromJson(new JsonPrimitive(1.5), ConfigType.LONG));
    }

    @Test
    void rejectsIntOutOfRange() {
        assertThrows(
                ConfigException.class, () -> ConfigValues.fromJson(new JsonPrimitive(Long.MAX_VALUE), ConfigType.INT));
    }

    @Test
    void acceptsWholeValuedDoubleAsInt() {
        assertEquals(4, (int) ConfigValues.<Integer>fromJson(new JsonPrimitive(4.0), ConfigType.INT));
    }
}

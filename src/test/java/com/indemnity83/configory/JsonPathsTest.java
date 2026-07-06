package com.indemnity83.configory;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;

class JsonPathsTest {

    @Test
    void setThenGetNestedValue() {
        JsonObject root = new JsonObject();
        ConfigPath path = ConfigPath.parse("engines.stirling.max_output");
        JsonPaths.set(root, path, new JsonPrimitive(10.0));

        assertEquals(10.0, JsonPaths.get(root, path).getAsDouble());
        assertTrue(root.getAsJsonObject("stirling").has("max_output"));
    }

    @Test
    void getReturnsNullForMissingPath() {
        JsonObject root = new JsonObject();
        assertNull(JsonPaths.get(root, ConfigPath.parse("a.b.c")));
    }

    @Test
    void getReturnsNullWhenIntermediateIsNotObject() {
        JsonObject root = new JsonObject();
        root.addProperty("a", 3);
        assertNull(JsonPaths.get(root, ConfigPath.parse("file.a.b")));
    }

    @Test
    void setOverwritesNonObjectIntermediate() {
        JsonObject root = new JsonObject();
        root.addProperty("a", 3);
        ConfigPath path = ConfigPath.parse("file.a.b");
        JsonPaths.set(root, path, new JsonPrimitive(7));
        assertEquals(7, JsonPaths.get(root, path).getAsInt());
    }

    @Test
    void setShallowValueUsesFirstSegment() {
        JsonObject root = new JsonObject();
        ConfigPath path = ConfigPath.parse("file.key");
        JsonPaths.set(root, path, new JsonPrimitive("v"));
        assertEquals("v", root.get("key").getAsString());
    }
}

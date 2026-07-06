package com.indemnity83.configory.storage;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.indemnity83.configory.ConfigException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JsonFileConfigStorageTest {

    @TempDir
    Path root;

    @Test
    void loadReturnsEmptyDocumentForMissingFile() {
        JsonFileConfigStorage storage = new JsonFileConfigStorage(root);
        assertTrue(storage.load("core").entrySet().isEmpty());
    }

    @Test
    void loadReturnsEmptyDocumentForEmptyFile() throws IOException {
        Files.writeString(root.resolve("core.json"), "");
        JsonFileConfigStorage storage = new JsonFileConfigStorage(root);
        assertTrue(storage.load("core").entrySet().isEmpty());
    }

    @Test
    void loadThrowsConfigExceptionForMalformedJson() throws IOException {
        Files.writeString(root.resolve("core.json"), "{ not valid json ");
        JsonFileConfigStorage storage = new JsonFileConfigStorage(root);

        ConfigException ex = assertThrows(ConfigException.class, () -> storage.load("core"));
        assertTrue(ex.getMessage().contains("core"));
    }

    @Test
    void saveThenLoadRoundTripsContent() {
        JsonFileConfigStorage storage = new JsonFileConfigStorage(root);
        JsonObject document = new JsonObject();
        document.add("speed", new JsonPrimitive(3.0));

        storage.save("core", document);

        assertEquals(3.0, storage.load("core").get("speed").getAsDouble());
    }

    @Test
    void saveCreatesMissingParentDirectories() {
        Path nestedRoot = root.resolve("deeply").resolve("nested");
        JsonFileConfigStorage storage = new JsonFileConfigStorage(nestedRoot);

        storage.save("core", new JsonObject());

        assertTrue(Files.exists(nestedRoot.resolve("core.json")));
    }
}

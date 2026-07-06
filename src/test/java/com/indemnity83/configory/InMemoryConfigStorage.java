package com.indemnity83.configory;

import com.google.gson.JsonObject;
import com.indemnity83.configory.storage.ConfigStorage;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link ConfigStorage} that keeps documents in memory, so tests never touch the filesystem.
 * Documents are deep-copied on the way in and out so that mutations to the live in-memory
 * {@link Config} document do not leak into "persisted" state until {@code save()} is called.
 */
final class InMemoryConfigStorage implements ConfigStorage {
    private final Map<String, JsonObject> files = new HashMap<>();
    int loadCount = 0;
    int saveCount = 0;

    @Override
    public JsonObject load(String file) {
        loadCount++;
        JsonObject stored = files.get(file);
        return stored == null ? new JsonObject() : stored.deepCopy();
    }

    @Override
    public void save(String file, JsonObject root) {
        saveCount++;
        files.put(file, root.deepCopy());
    }

    /** Pre-populate a file without counting it as a save, for arranging test state. */
    void seed(String file, JsonObject document) {
        files.put(file, document.deepCopy());
    }

    boolean has(String file) {
        return files.containsKey(file);
    }

    JsonObject raw(String file) {
        return files.get(file);
    }
}

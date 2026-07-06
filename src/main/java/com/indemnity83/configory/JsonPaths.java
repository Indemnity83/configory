package com.indemnity83.configory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Reads and writes nested values in a JSON document addressed by a {@link ConfigPath}.
 *
 * <p>Internal to Configory; not part of the public API.
 */
final class JsonPaths {
    private JsonPaths() {}

    static JsonElement get(JsonObject root, ConfigPath path) {
        JsonElement current = root;
        for (String segment : path.segments()) {
            if (!(current instanceof JsonObject object)) {
                return null;
            }
            current = object.get(segment);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    static void set(JsonObject root, ConfigPath path, JsonElement value) {
        JsonObject current = root;
        for (int i = 0; i < path.segments().size() - 1; i++) {
            current = childObject(current, path.segments().get(i));
        }
        current.add(path.segments().getLast(), value);
    }

    private static JsonObject childObject(JsonObject parent, String segment) {
        if (parent.get(segment) instanceof JsonObject object) {
            return object;
        }
        JsonObject child = new JsonObject();
        parent.add(segment, child);
        return child;
    }
}

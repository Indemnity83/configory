package com.indemnity83.configory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class JsonPaths {
    private JsonPaths() {}

    public static JsonElement get(JsonObject root, ConfigPath path) {
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

    public static void set(JsonObject root, ConfigPath path, JsonElement value) {
        JsonObject current = root;
        for (int i = 0; i < path.segments().size() - 1; i++) {
            String segment = path.segments().get(i);
            JsonElement existing = current.get(segment);
            JsonObject child;
            if (existing instanceof JsonObject object) {
                child = object;
            } else {
                child = new JsonObject();
                current.add(segment, child);
            }
            current = child;
        }
        current.add(path.segments().getLast(), value);
    }
}

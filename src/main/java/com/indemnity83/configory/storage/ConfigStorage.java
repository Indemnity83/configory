package com.indemnity83.configory.storage;

import com.google.gson.JsonObject;

public interface ConfigStorage {
    JsonObject load(String file);

    void save(String file, JsonObject root);
}

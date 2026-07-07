package com.indemnity83.configory.storage;

import com.google.gson.JsonObject;

/**
 * Persistence backend for a config's files (a service provider interface).
 *
 * <p>A config persists its single document through an implementation of this interface, keyed by its
 * (possibly dotted) config id. The default is {@link JsonFileConfigStorage}, which maps the id to a
 * {@code .json} file on disk (dots become subdirectories); supply a custom implementation to
 * {@link com.indemnity83.configory.Config#create(String, ConfigStorage)} to back a config with
 * something else (for example an in-memory store in tests).
 */
public interface ConfigStorage {
    /**
     * Loads a file's document.
     *
     * @param file the config id (may be dotted)
     * @return the stored document, or a new empty {@link JsonObject} if the file does not exist
     * @throws com.indemnity83.configory.ConfigException if the file exists but cannot be read or
     *     parsed
     */
    JsonObject load(String file);

    /**
     * Persists a file's document, replacing any prior contents.
     *
     * @param file the config id (may be dotted)
     * @param root the document to write
     * @throws com.indemnity83.configory.ConfigException if the document cannot be written
     */
    void save(String file, JsonObject root);
}

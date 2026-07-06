package com.indemnity83.configory.storage;

import com.google.gson.JsonObject;

/**
 * Persistence backend for a config's files (a service provider interface).
 *
 * <p>Each config maps file names to JSON documents through an implementation of this interface. The
 * default is {@link JsonFileConfigStorage}, which reads and writes {@code <file>.json} on disk; supply
 * a custom implementation to {@link com.indemnity83.configory.Config#create(String, ConfigStorage)}
 * to back a config with something else (for example an in-memory store in tests).
 */
public interface ConfigStorage {
    /**
     * Loads a file's document.
     *
     * @param file the file name (without extension)
     * @return the stored document, or a new empty {@link JsonObject} if the file does not exist
     * @throws com.indemnity83.configory.ConfigException if the file exists but cannot be read or
     *     parsed
     */
    JsonObject load(String file);

    /**
     * Persists a file's document, replacing any prior contents.
     *
     * @param file the file name (without extension)
     * @param root the document to write
     * @throws com.indemnity83.configory.ConfigException if the document cannot be written
     */
    void save(String file, JsonObject root);
}

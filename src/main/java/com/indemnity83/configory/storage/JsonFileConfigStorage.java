package com.indemnity83.configory.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.indemnity83.configory.ConfigException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The default {@link ConfigStorage}: stores each file as a pretty-printed {@code <file>.json} under a
 * root directory.
 *
 * <p>Files are resolved as {@code <rootDirectory>/<file>.json}. Loading a missing file yields an
 * empty document, and saving creates any missing parent directories. This is the storage
 * {@link com.indemnity83.configory.Config#create(String)} uses.
 */
public final class JsonFileConfigStorage implements ConfigStorage {
    private final Path rootDirectory;
    private final Gson gson;

    /**
     * Creates a storage rooted at the given directory.
     *
     * @param rootDirectory the directory under which {@code <file>.json} files are read and written
     */
    public JsonFileConfigStorage(Path rootDirectory) {
        this.rootDirectory = rootDirectory;
        this.gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns an empty document when the file does not exist.
     *
     * @throws ConfigException if the file exists but cannot be read or parsed as JSON
     */
    @Override
    public JsonObject load(String file) {
        Path path = pathFor(file);
        if (!Files.exists(path)) {
            return new JsonObject();
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject object = gson.fromJson(reader, JsonObject.class);
            return object == null ? new JsonObject() : object;
        } catch (IOException | JsonParseException e) {
            throw new ConfigException("Failed to load config file: " + path, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Creates any missing parent directories before writing.
     *
     * @throws ConfigException if the file cannot be written
     */
    @Override
    public void save(String file, JsonObject root) {
        Path path = pathFor(file);
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                gson.toJson(root, writer);
            }
        } catch (IOException e) {
            throw new ConfigException("Failed to save config file: " + path, e);
        }
    }

    private Path pathFor(String file) {
        return rootDirectory.resolve(file + ".json");
    }
}

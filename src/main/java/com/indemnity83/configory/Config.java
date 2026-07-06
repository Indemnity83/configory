package com.indemnity83.configory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.indemnity83.configory.builder.ConfigDefinitionBuilder;
import com.indemnity83.configory.storage.ConfigStorage;
import com.indemnity83.configory.storage.JsonFileConfigStorage;
import java.nio.file.Path;
import java.util.*;

/**
 * A single mod's isolated configuration namespace.
 *
 * <p>A {@code Config} owns a set of typed {@link ConfigDefinition definitions}, an in-memory copy of
 * each backing JSON document, and the set of files with unsaved runtime changes. Values are
 * addressed by dotted paths (see {@link ConfigPath}): the first segment names the file and the
 * remaining segments traverse the JSON tree, while a bare key with no dot lands in the reserved
 * default {@code config.json}.
 *
 * <p>The typical lifecycle is: declare keys with {@link #define(String)}, call {@link #load()} once
 * to read files from disk (applying defaults, validating, and running sanitize hooks), then read
 * with {@link #get(ConfigKey)} / {@link #get(String)} and mutate with {@link #set(ConfigKey, Object)}
 * / {@link #set(String, Object)}. Mutations only update the in-memory document and mark the file
 * dirty; changes are written to disk only when {@link #save()} (or {@link #save(String)}) is called.
 *
 * <p>Instances are usually obtained from {@link ConfigRegistry} rather than constructed directly, so
 * that each mod id maps to exactly one shared instance.
 */
public final class Config {
    private final String id;
    private final ConfigStorage storage;
    private final Map<String, JsonObject> documents = new HashMap<>();
    private final Set<String> dirtyFiles = new HashSet<>();
    private final Map<ConfigPath, ConfigDefinition<?>> definitions = new LinkedHashMap<>();
    private final List<Runnable> sanitizeHooks = new ArrayList<>();

    private Config(String id, ConfigStorage storage) {
        this.id = Objects.requireNonNull(id, "id");
        this.storage = Objects.requireNonNull(storage, "storage");
    }

    /**
     * Creates a config backed by JSON files under {@code config/<id>/} relative to the working
     * directory (the Minecraft config directory at runtime).
     *
     * @param id the mod id; also the folder name that isolates this config from other mods
     * @return a new, empty config with no definitions registered and nothing loaded yet
     */
    public static Config create(String id) {
        Path defaultRoot = Path.of("config").resolve(id);
        return new Config(id, new JsonFileConfigStorage(defaultRoot));
    }

    /**
     * Creates a config backed by the supplied {@link ConfigStorage}, allowing custom persistence
     * (for example an in-memory store in tests).
     *
     * @param id the mod id used to scope and identify this config
     * @param storage the storage implementation that loads and saves each file's JSON document
     * @return a new, empty config with no definitions registered and nothing loaded yet
     */
    public static Config create(String id, ConfigStorage storage) {
        return new Config(id, storage);
    }

    /**
     * {@return the mod id this config is scoped to}
     */
    public String id() {
        return id;
    }

    /**
     * Begins a fluent definition for the value at the given path.
     *
     * <p>Continue the chain by choosing a type ({@code asFloat()}, {@code asInt()}, ...), a default
     * value, optional constraints, and finally {@code register()} to obtain a {@link ConfigKey}.
     *
     * @param path a dotted path such as {@code "core.speed_multiplier"} or a bare key such as
     *     {@code "speed_multiplier"}; see {@link ConfigPath} for how paths map to files
     * @return a builder for declaring the value's type, default, and validation
     */
    public ConfigDefinitionBuilder define(String path) {
        return new ConfigDefinitionBuilder(this, ConfigPath.parse(path));
    }

    /**
     * Registers a completed definition and returns a typed key bound to this config.
     *
     * <p>Called by the builder's {@code register()}; rarely invoked directly.
     *
     * @param definition the definition to register
     * @param <T> the value type
     * @return a {@link ConfigKey} that can be used with {@link #get(ConfigKey)} and
     *     {@link #set(ConfigKey, Object)}
     * @throws ConfigException if a definition is already registered at the same path
     */
    public <T> ConfigKey<T> registerDefinition(ConfigDefinition<T> definition) {
        if (definitions.containsKey(definition.path())) {
            throw new ConfigException(
                    "Duplicate config definition: " + definition.path().fullPath());
        }
        definitions.put(definition.path(), definition);
        return new ConfigKey<>(id, definition);
    }

    /**
     * Reads the raw value at the given path as a {@link ConfigValue} for dynamic, untyped access.
     *
     * <p>No definition is required; the returned wrapper defers type coercion to its {@code asX}
     * accessors. If nothing is stored at the path, the returned value is empty and its {@code asX}
     * accessors either throw or return the supplied fallback.
     *
     * @param path a dotted path or bare key identifying the value
     * @return a {@link ConfigValue} wrapping whatever is currently stored (possibly absent)
     */
    public ConfigValue get(String path) {
        ConfigPath configPath = ConfigPath.parse(path);
        JsonObject document = document(configPath.file());
        JsonElement element = JsonPaths.get(document, configPath);
        return new ConfigValue(this, configPath, element);
    }

    /**
     * Reads the typed value for a registered key.
     *
     * <p>Returns the definition's default value when nothing is stored at the key's path. When a
     * value is present it is coerced to the key's type and validated against the key's constraints
     * before being returned.
     *
     * @param key a key registered with this config
     * @param <T> the value type
     * @return the stored value, or the definition's default when unset
     * @throws ConfigException if the key does not belong to this config
     * @throws ConfigValidationException if the stored value fails validation
     */
    public <T> T get(ConfigKey<T> key) {
        assertOwns(key);
        JsonObject document = document(key.path().file());
        JsonElement element = JsonPaths.get(document, key.path());
        if (element == null || element.isJsonNull()) {
            return key.definition().defaultValue();
        }
        T value = ConfigValues.fromJson(element, key.definition().type());
        validateOrThrow(key, value);
        return value;
    }

    /**
     * Writes a value at the given path in memory, marking the backing file dirty if the value
     * changed.
     *
     * <p>This does not validate against any definition and does not write to disk. Chain
     * {@link ConfigMutation#save()} on the result to persist just the affected file.
     *
     * @param path a dotted path or bare key identifying the value
     * @param value the new value; must be a supported primitive type (boolean, String, int, long,
     *     float, or double)
     * @return a {@link ConfigMutation} describing whether the value changed and allowing a chained
     *     save
     * @throws ConfigException if the value's type is not supported
     */
    public ConfigMutation set(String path, Object value) {
        ConfigPath configPath = ConfigPath.parse(path);
        return setRaw(configPath, value);
    }

    /**
     * Writes a typed value for a registered key after validating it, marking the backing file dirty
     * if the value changed.
     *
     * <p>Validation runs before the write, so invalid values are rejected rather than stored. The
     * change is in memory only until {@link #save()} or {@link ConfigMutation#save()} is called.
     *
     * @param key a key registered with this config
     * @param value the new value to validate and store
     * @param <T> the value type
     * @return a {@link ConfigMutation} describing whether the value changed and allowing a chained
     *     save
     * @throws ConfigException if the key does not belong to this config
     * @throws ConfigValidationException if the value fails the key's constraints
     */
    public <T> ConfigMutation set(ConfigKey<T> key, T value) {
        assertOwns(key);
        validateOrThrow(key, value);
        return setRaw(key.path(), value);
    }

    /**
     * Loads every file referenced by a registered definition from storage, replacing all in-memory
     * state.
     *
     * <p>After reading, this applies defaults for any missing keys, replaces values that are invalid
     * or of the wrong type with their defaults (marking those files dirty so the repaired values can
     * be saved back), and finally runs any registered sanitize hooks. Existing in-memory documents
     * and the dirty-file set are discarded first.
     */
    public void load() {
        documents.clear();
        dirtyFiles.clear();
        Set<String> files = new LinkedHashSet<>();
        for (ConfigDefinition<?> definition : definitions.values()) {
            files.add(definition.path().file());
        }
        for (String file : files) {
            documents.put(file, storage.load(file));
        }
        applyDefaultsAndValidate();
        runSanitizeHooks();
    }

    /**
     * Re-reads all config files from disk, but only when there are no unsaved changes.
     *
     * <p>This is the safe reload: it refuses to silently discard runtime mutations. To reload while
     * throwing away unsaved changes, use {@link #discardAndReload()}.
     *
     * @throws ConfigException if any file is dirty; save or discard the changes first
     */
    public void reload() {
        if (!dirtyFiles.isEmpty()) {
            throw new ConfigException("Cannot reload config '" + id + "' because unsaved changes exist: " + dirtyFiles
                    + ". Call save() or discardAndReload().");
        }
        load();
    }

    /**
     * Discards all unsaved runtime changes and re-reads every config file from disk.
     *
     * <p>Unlike {@link #reload()} this never throws on dirty state; any in-memory mutations that were
     * not saved are lost.
     */
    public void discardAndReload() {
        documents.clear();
        dirtyFiles.clear();
        load();
    }

    /**
     * Writes every dirty file to storage and clears the dirty set.
     *
     * <p>Files with no pending changes are not touched.
     */
    public void save() {
        for (String file : List.copyOf(dirtyFiles)) {
            save(file);
        }
    }

    /**
     * Writes a single file to storage and clears its dirty flag.
     *
     * <p>Does nothing if no in-memory document exists for the given file.
     *
     * @param file the file name (first path segment, without the {@code .json} extension)
     */
    public void save(String file) {
        JsonObject document = documents.get(file);
        if (document == null) {
            return;
        }
        storage.save(file, document);
        dirtyFiles.remove(file);
    }

    /**
     * {@return whether any file has unsaved runtime changes}
     */
    public boolean isDirty() {
        return !dirtyFiles.isEmpty();
    }

    /**
     * {@return an immutable snapshot of the file names with unsaved changes}
     */
    public Set<String> dirtyFiles() {
        return Set.copyOf(dirtyFiles);
    }

    /**
     * Registers a hook run at the end of every {@link #load()} (after defaults and validation).
     *
     * <p>Use hooks to repair cross-field invariants that no single-key constraint can express, for
     * example keeping a min/max pair ordered via {@link #repairMinMax(ConfigKey, ConfigKey)}. Hooks
     * are typically registered from the optional {@code Configs.bootstrap(Config)} method.
     *
     * @param hook the action to run after each load
     */
    public void registerSanitizeHook(Runnable hook) {
        sanitizeHooks.add(hook);
    }

    /**
     * Ensures a min/max pair is ordered, raising the minimum to equal the maximum when it exceeds it.
     *
     * <p>Intended for use inside a sanitize hook. If {@code min <= max} nothing changes; otherwise the
     * min key is set to the max value (marking its file dirty).
     *
     * @param minKey the key holding the lower bound
     * @param maxKey the key holding the upper bound
     * @param <T> the numeric, comparable value type
     */
    public <T extends Number & Comparable<T>> void repairMinMax(ConfigKey<T> minKey, ConfigKey<T> maxKey) {
        T min = get(minKey);
        T max = get(maxKey);
        if (min.compareTo(max) <= 0) {
            return;
        }
        set(minKey, max);
    }

    /**
     * {@return an unmodifiable view of all registered definitions, in registration order}
     *
     * <p>Useful for tooling such as generated documentation or config screens.
     */
    public Collection<ConfigDefinition<?>> definitions() {
        return Collections.unmodifiableCollection(definitions.values());
    }

    private ConfigMutation setRaw(ConfigPath path, Object value) {
        JsonObject document = document(path.file());
        JsonElement previous = JsonPaths.get(document, path);
        JsonElement next = ConfigValues.toJson(value);
        boolean changed = previous == null || !previous.equals(next);
        if (changed) {
            JsonPaths.set(document, path, next);
            dirtyFiles.add(path.file());
        }
        return new ConfigMutation(this, path, changed);
    }

    private JsonObject document(String file) {
        return documents.computeIfAbsent(file, storage::load);
    }

    private void applyDefaultsAndValidate() {
        for (ConfigDefinition<?> definition : definitions.values()) {
            applyDefaultAndValidateUnchecked(definition);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> void applyDefaultAndValidateUnchecked(ConfigDefinition<T> definition) {
        JsonObject document = document(definition.path().file());
        JsonElement element = JsonPaths.get(document, definition.path());
        if (element == null || element.isJsonNull()) {
            JsonPaths.set(document, definition.path(), ConfigValues.toJson(definition.defaultValue()));
            dirtyFiles.add(definition.path().file());
            return;
        }
        try {
            T value = ConfigValues.fromJson(element, definition.type());
            ValidationResult result = definition.validate(value, this);
            if (!result.valid()) {
                JsonPaths.set(document, definition.path(), ConfigValues.toJson(definition.defaultValue()));
                dirtyFiles.add(definition.path().file());
            }
        } catch (ConfigException e) {
            JsonPaths.set(document, definition.path(), ConfigValues.toJson(definition.defaultValue()));
            dirtyFiles.add(definition.path().file());
        }
    }

    private void runSanitizeHooks() {
        for (Runnable hook : sanitizeHooks) {
            hook.run();
        }
    }

    private void assertOwns(ConfigKey<?> key) {
        if (!id.equals(key.configId())) {
            throw new ConfigException("Config key " + key + " does not belong to config '" + id + "'.");
        }
    }

    private <T> void validateOrThrow(ConfigKey<T> key, T value) {
        ValidationResult result = key.definition().validate(value, this);
        if (!result.valid()) {
            throw new ConfigValidationException(
                    "Invalid config value at " + key.path().fullPath() + ": " + result.message());
        }
    }
}

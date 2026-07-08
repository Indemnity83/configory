package com.indemnity83.configory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.indemnity83.configory.builder.BooleanConfigBuilder;
import com.indemnity83.configory.builder.ConfigDefinitionBuilder;
import com.indemnity83.configory.builder.DoubleConfigBuilder;
import com.indemnity83.configory.builder.EnumConfigBuilder;
import com.indemnity83.configory.builder.FloatConfigBuilder;
import com.indemnity83.configory.builder.IntConfigBuilder;
import com.indemnity83.configory.builder.LongConfigBuilder;
import com.indemnity83.configory.builder.StringConfigBuilder;
import com.indemnity83.configory.storage.ConfigStorage;
import com.indemnity83.configory.storage.JsonFileConfigStorage;
import java.nio.file.Path;
import java.util.*;

/**
 * A single JSON file's worth of typed configuration.
 *
 * <p>A {@code Config} owns a set of typed {@link ConfigDefinition definitions} and an in-memory copy
 * of one backing JSON document. Values are addressed by dotted {@link ConfigPath paths} whose every
 * dot is a nesting boundary. Which file the document lives in is decided by the {@linkplain #id()
 * config id}: {@code "examplemod"} maps to {@code config/examplemod.json} and
 * {@code "examplemod.engines"} to {@code config/examplemod/engines.json}.
 *
 * <p>The typical lifecycle is: declare keys with {@link #define(String)}, call {@link #load()} once
 * to read the file from disk (applying defaults, validating, and running sanitize hooks), then read
 * with {@link #get(ConfigKey)} / {@link #get(String)} and mutate with {@link #set(ConfigKey, Object)}
 * / {@link #set(String, Object)}. Mutations only update the in-memory document and mark the config
 * dirty; changes are written to disk only when {@link #save()} is called.
 *
 * <p>Instances are usually obtained from {@link ConfigRegistry} rather than constructed directly, so
 * that each id maps to exactly one shared instance.
 *
 * <p><strong>Threading:</strong> a {@code Config} is not thread-safe. The registry hands out one
 * shared instance per id, but a given instance expects single-threaded use (the common case: load on
 * the mod's init thread, then read/write from game logic on the server thread). Guard it externally
 * if you must touch the same config from multiple threads.
 */
public final class Config {
    private final String id;
    private final ConfigStorage storage;
    private JsonObject document;
    private boolean dirty;
    private final Map<ConfigPath, ConfigDefinition<?>> definitions = new LinkedHashMap<>();
    private final List<Runnable> sanitizeHooks = new ArrayList<>();
    private final Set<ConfigPath> validating = new HashSet<>();

    private Config(String id, ConfigStorage storage) {
        this.id = Objects.requireNonNull(id, "id");
        this.storage = Objects.requireNonNull(storage, "storage");
    }

    /**
     * Creates a config backed by a JSON file under {@code config/} relative to the working directory
     * (the Minecraft config directory at runtime). The id decides the file: {@code "examplemod"} →
     * {@code config/examplemod.json}, {@code "examplemod.engines"} →
     * {@code config/examplemod/engines.json}.
     *
     * @param id the config id; its dots become subdirectories, its last segment the file name
     * @return a new, empty config with no definitions registered and nothing loaded yet
     * @throws ConfigException if the id is blank or has an unsafe segment
     */
    public static Config create(String id) {
        validateId(id);
        return new Config(id, new JsonFileConfigStorage(Path.of("config")));
    }

    /**
     * Creates a config backed by the supplied {@link ConfigStorage}, allowing custom persistence
     * (for example an in-memory store in tests).
     *
     * @param id the config id used to scope and identify this config
     * @param storage the storage implementation that loads and saves the config's JSON document
     * @return a new, empty config with no definitions registered and nothing loaded yet
     * @throws ConfigException if the id is blank or has an unsafe segment
     */
    public static Config create(String id, ConfigStorage storage) {
        validateId(id);
        return new Config(id, storage);
    }

    /**
     * {@return the id this config is scoped to (also its file location)}
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
     *     {@code "speed_multiplier"}; every dot is a nesting boundary (see {@link ConfigPath})
     * @return a builder for declaring the value's type, default, and validation
     */
    public ConfigDefinitionBuilder define(String path) {
        return new ConfigDefinitionBuilder(this, ConfigPath.parse(path));
    }

    /**
     * Begins a fluent definition for a boolean value at the given path.
     *
     * <p>Shorthand for {@code define(path).asBoolean()}.
     *
     * @param path a dotted path or bare key; see {@link ConfigPath}
     * @return a boolean builder for setting a default, optional constraints, and registering
     */
    public BooleanConfigBuilder defineBoolean(String path) {
        return define(path).asBoolean();
    }

    /**
     * Begins a fluent definition for a boolean value with its default already set.
     *
     * <p>Shorthand for {@code define(path).asBoolean().defaultValue(defaultValue)}.
     *
     * @param path a dotted path or bare key; see {@link ConfigPath}
     * @param defaultValue the value used when the key is unset or a stored value is invalid
     * @return a boolean builder for adding optional constraints and registering
     */
    public BooleanConfigBuilder defineBoolean(String path, boolean defaultValue) {
        return defineBoolean(path).defaultValue(defaultValue);
    }

    /**
     * Begins a fluent definition for a string value at the given path.
     *
     * <p>Shorthand for {@code define(path).asString()}.
     *
     * @param path a dotted path or bare key; see {@link ConfigPath}
     * @return a string builder for setting a default, optional constraints, and registering
     */
    public StringConfigBuilder defineString(String path) {
        return define(path).asString();
    }

    /**
     * Begins a fluent definition for a string value with its default already set.
     *
     * <p>Shorthand for {@code define(path).asString().defaultValue(defaultValue)}.
     *
     * @param path a dotted path or bare key; see {@link ConfigPath}
     * @param defaultValue the value used when the key is unset or a stored value is invalid
     * @return a string builder for adding optional constraints and registering
     */
    public StringConfigBuilder defineString(String path, String defaultValue) {
        return defineString(path).defaultValue(defaultValue);
    }

    /**
     * Begins a fluent definition for an int value at the given path.
     *
     * <p>Shorthand for {@code define(path).asInt()}.
     *
     * @param path a dotted path or bare key; see {@link ConfigPath}
     * @return an int builder for setting a default, optional constraints, and registering
     */
    public IntConfigBuilder defineInt(String path) {
        return define(path).asInt();
    }

    /**
     * Begins a fluent definition for an int value with its default already set.
     *
     * <p>Shorthand for {@code define(path).asInt().defaultValue(defaultValue)}.
     *
     * @param path a dotted path or bare key; see {@link ConfigPath}
     * @param defaultValue the value used when the key is unset or a stored value is invalid
     * @return an int builder for adding optional constraints and registering
     */
    public IntConfigBuilder defineInt(String path, int defaultValue) {
        return defineInt(path).defaultValue(defaultValue);
    }

    /**
     * Begins a fluent definition for a long value at the given path.
     *
     * <p>Shorthand for {@code define(path).asLong()}.
     *
     * @param path a dotted path or bare key; see {@link ConfigPath}
     * @return a long builder for setting a default, optional constraints, and registering
     */
    public LongConfigBuilder defineLong(String path) {
        return define(path).asLong();
    }

    /**
     * Begins a fluent definition for a long value with its default already set.
     *
     * <p>Shorthand for {@code define(path).asLong().defaultValue(defaultValue)}.
     *
     * @param path a dotted path or bare key; see {@link ConfigPath}
     * @param defaultValue the value used when the key is unset or a stored value is invalid
     * @return a long builder for adding optional constraints and registering
     */
    public LongConfigBuilder defineLong(String path, long defaultValue) {
        return defineLong(path).defaultValue(defaultValue);
    }

    /**
     * Begins a fluent definition for a float value at the given path.
     *
     * <p>Shorthand for {@code define(path).asFloat()}.
     *
     * @param path a dotted path or bare key; see {@link ConfigPath}
     * @return a float builder for setting a default, optional constraints, and registering
     */
    public FloatConfigBuilder defineFloat(String path) {
        return define(path).asFloat();
    }

    /**
     * Begins a fluent definition for a float value with its default already set.
     *
     * <p>Shorthand for {@code define(path).asFloat().defaultValue(defaultValue)}.
     *
     * @param path a dotted path or bare key; see {@link ConfigPath}
     * @param defaultValue the value used when the key is unset or a stored value is invalid
     * @return a float builder for adding optional constraints and registering
     */
    public FloatConfigBuilder defineFloat(String path, float defaultValue) {
        return defineFloat(path).defaultValue(defaultValue);
    }

    /**
     * Begins a fluent definition for a double value at the given path.
     *
     * <p>Shorthand for {@code define(path).asDouble()}.
     *
     * @param path a dotted path or bare key; see {@link ConfigPath}
     * @return a double builder for setting a default, optional constraints, and registering
     */
    public DoubleConfigBuilder defineDouble(String path) {
        return define(path).asDouble();
    }

    /**
     * Begins a fluent definition for a double value with its default already set.
     *
     * <p>Shorthand for {@code define(path).asDouble().defaultValue(defaultValue)}.
     *
     * @param path a dotted path or bare key; see {@link ConfigPath}
     * @param defaultValue the value used when the key is unset or a stored value is invalid
     * @return a double builder for adding optional constraints and registering
     */
    public DoubleConfigBuilder defineDouble(String path, double defaultValue) {
        return defineDouble(path).defaultValue(defaultValue);
    }

    /**
     * Begins a fluent definition for an enum value at the given path.
     *
     * <p>Shorthand for {@code define(path).asEnum(enumClass)}. The value is stored by its
     * {@code name()}.
     *
     * @param path a dotted path or bare key; see {@link ConfigPath}
     * @param enumClass the enum type
     * @param <E> the enum type
     * @return an enum builder for setting a default, optional constraints, and registering
     */
    public <E extends Enum<E>> EnumConfigBuilder<E> defineEnum(String path, Class<E> enumClass) {
        return define(path).asEnum(enumClass);
    }

    /**
     * Begins a fluent definition for an enum value with its default already set.
     *
     * <p>Shorthand for {@code define(path).asEnum(...).defaultValue(defaultValue)}; the enum type is
     * inferred from the default.
     *
     * @param path a dotted path or bare key; see {@link ConfigPath}
     * @param defaultValue the value used when the key is unset or a stored value is invalid
     * @param <E> the enum type
     * @return an enum builder for adding optional constraints and registering
     */
    public <E extends Enum<E>> EnumConfigBuilder<E> defineEnum(String path, E defaultValue) {
        return defineEnum(path, defaultValue.getDeclaringClass()).defaultValue(defaultValue);
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
        JsonObject document = documentOrLoad();
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
        JsonObject document = documentOrLoad();
        JsonElement element = JsonPaths.get(document, key.path());
        if (element == null || element.isJsonNull()) {
            return key.definition().defaultValue();
        }
        T value = ConfigValues.fromJson(
                element, key.definition().type(), key.definition().valueClass());
        // Skip validation while this key is already being validated higher on the stack, so mutually
        // referential cross-field validators (e.g. a minValueOf/maxValueOf pair) read each other's
        // value instead of recursing until the stack overflows.
        if (validating.add(key.path())) {
            try {
                validateOrThrow(key, value);
            } finally {
                validating.remove(key.path());
            }
        }
        return value;
    }

    /**
     * Writes a value at the given path in memory, marking the config dirty if the value
     * changed.
     *
     * <p>This does not validate against any definition and does not write to disk. Chain
     * {@link ConfigMutation#save()} on the result to persist the config. Use
     * {@link #trySet(String, Object)} instead to reject values that violate a definition's
     * constraints.
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
     * Writes a typed value for a registered key after validating it, marking the config dirty
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
     * Writes a value at the given path only if it satisfies the constraints of a definition
     * registered there, reporting success instead of throwing.
     *
     * <p>This is the validating counterpart to {@link #set(String, Object)} for dynamic tools —
     * commands, debug menus, generated screens — that take untrusted input and need to branch on
     * whether it was accepted. When a definition exists at the path, the value is coerced to the
     * definition's type and checked against its constraints; if coercion or validation fails,
     * nothing is written and {@code false} is returned. When no definition exists there are no
     * constraints to apply, so the value is written and {@code true} is returned. As with
     * {@link #set(String, Object)}, the change is in memory only until a {@code save}.
     *
     * @param path a dotted path or bare key identifying the value
     * @param value the candidate value
     * @return {@code true} if the value was valid and written, {@code false} if it was rejected
     */
    public boolean trySet(String path, Object value) {
        ConfigPath configPath = ConfigPath.parse(path);
        ConfigDefinition<?> definition = definitions.get(configPath);
        if (definition != null && !isValidFor(definition, value)) {
            return false;
        }
        setRaw(configPath, value);
        return true;
    }

    /**
     * Writes a typed value for a registered key only if it satisfies the key's constraints,
     * reporting success instead of throwing.
     *
     * <p>The validating counterpart to {@link #set(ConfigKey, Object)}: on a constraint failure
     * nothing is written and {@code false} is returned rather than a
     * {@link ConfigValidationException}. The change is in memory only until a {@code save}.
     *
     * @param key a key registered with this config
     * @param value the candidate value
     * @param <T> the value type
     * @return {@code true} if the value was valid and written, {@code false} if it was rejected
     * @throws ConfigException if the key does not belong to this config
     */
    public <T> boolean trySet(ConfigKey<T> key, T value) {
        assertOwns(key);
        if (!key.definition().validate(value, this).valid()) {
            return false;
        }
        setRaw(key.path(), value);
        return true;
    }

    /**
     * Loads this config's file from storage, replacing all in-memory state.
     *
     * <p>After reading, this applies defaults for any missing keys, replaces values that are invalid
     * or of the wrong type with their defaults (marking the config dirty so the repaired values can
     * be saved back), and finally runs any registered sanitize hooks.
     *
     * @return this config, for chaining — e.g. {@code config.load().save()} to persist any defaults
     *     applied for a fresh install ({@link #save()} is a no-op when nothing changed)
     */
    public Config load() {
        document = storage.load(id);
        dirty = false;
        applyDefaultsAndValidate();
        runSanitizeHooks();
        return this;
    }

    /**
     * Re-reads this config's file from disk, but only when there are no unsaved changes.
     *
     * <p>This is the safe reload: it refuses to silently discard runtime mutations. To reload while
     * throwing away unsaved changes, use {@link #discardAndReload()}.
     *
     * @throws ConfigException if the config is dirty; save or discard the changes first
     */
    public void reload() {
        if (dirty) {
            throw new ConfigException("Cannot reload config '" + id
                    + "' because it has unsaved changes. Call save() or discardAndReload().");
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
        load();
    }

    /**
     * Writes this config's file to storage and clears its dirty flag.
     *
     * <p>Does nothing if there are no pending changes.
     */
    public void save() {
        if (dirty && document != null) {
            storage.save(id, document);
            dirty = false;
        }
    }

    /**
     * {@return whether this config has unsaved runtime changes}
     */
    public boolean isDirty() {
        return dirty;
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
     * min key is set to the max value (marking the config dirty).
     *
     * <p>The pair is read and repaired <em>without</em> validation, so this composes with
     * {@code minValueOf}/{@code maxValueOf} cross-field constraints on the same keys: an inverted pair
     * is exactly what those validators reject, and this repairs it rather than throwing.
     *
     * @param minKey the key holding the lower bound
     * @param maxKey the key holding the upper bound
     * @param <T> the numeric, comparable value type
     */
    public <T extends Number & Comparable<T>> void repairMinMax(ConfigKey<T> minKey, ConfigKey<T> maxKey) {
        T min = rawValue(minKey);
        T max = rawValue(maxKey);
        if (min.compareTo(max) <= 0) {
            return;
        }
        setRaw(minKey.path(), max);
    }

    private <T> T rawValue(ConfigKey<T> key) {
        assertOwns(key);
        JsonElement element = JsonPaths.get(documentOrLoad(), key.path());
        if (element == null || element.isJsonNull()) {
            return key.definition().defaultValue();
        }
        try {
            return ConfigValues.fromJson(
                    element, key.definition().type(), key.definition().valueClass());
        } catch (ConfigException e) {
            return key.definition().defaultValue();
        }
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
        JsonObject document = documentOrLoad();
        JsonElement previous = JsonPaths.get(document, path);
        JsonElement next = ConfigValues.toJson(value);
        boolean changed = previous == null || !previous.equals(next);
        if (changed) {
            JsonPaths.set(document, path, next);
            dirty = true;
        }
        return new ConfigMutation(this, path, changed);
    }

    private JsonObject documentOrLoad() {
        if (document == null) {
            document = storage.load(id);
        }
        return document;
    }

    private void applyDefaultsAndValidate() {
        for (ConfigDefinition<?> definition : definitions.values()) {
            validateOrApplyDefault(definition);
        }
    }

    private <T> void validateOrApplyDefault(ConfigDefinition<T> definition) {
        JsonObject document = documentOrLoad();
        JsonElement element = JsonPaths.get(document, definition.path());
        if (element == null || element.isJsonNull()) {
            writeDefault(document, definition);
            return;
        }
        try {
            T value = ConfigValues.fromJson(element, definition.type(), definition.valueClass());
            ValidationResult result = definition.validate(value, this);
            if (!result.valid()) {
                writeDefault(document, definition);
            }
        } catch (ConfigException e) {
            writeDefault(document, definition);
        }
    }

    private void writeDefault(JsonObject document, ConfigDefinition<?> definition) {
        JsonPaths.set(document, definition.path(), ConfigValues.toJson(definition.defaultValue()));
        dirty = true;
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

    private static void validateId(String id) {
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) {
            throw new ConfigException("Config id cannot be blank.");
        }
        for (String segment : id.split("\\.", -1)) {
            if (ConfigPath.isUnsafeSegment(segment)) {
                throw new ConfigException("Invalid config id (unsafe or empty segment): " + id);
            }
        }
    }

    private <T> boolean isValidFor(ConfigDefinition<T> definition, Object value) {
        T coerced;
        try {
            coerced = definition
                    .valueClass()
                    .cast(ConfigValues.fromJson(
                            ConfigValues.toJson(value), definition.type(), definition.valueClass()));
        } catch (ConfigException e) {
            return false;
        }
        return definition.validate(coerced, this).valid();
    }
}

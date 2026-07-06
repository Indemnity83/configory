package com.indemnity83.configory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.indemnity83.configory.builder.ConfigDefinitionBuilder;
import com.indemnity83.configory.storage.ConfigStorage;
import com.indemnity83.configory.storage.JsonFileConfigStorage;
import java.nio.file.Path;
import java.util.*;

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

    public static Config create(String id) {
        Path defaultRoot = Path.of("config").resolve(id);
        return new Config(id, new JsonFileConfigStorage(defaultRoot));
    }

    public static Config create(String id, ConfigStorage storage) {
        return new Config(id, storage);
    }

    public String id() {
        return id;
    }

    public ConfigDefinitionBuilder define(String path) {
        return new ConfigDefinitionBuilder(this, ConfigPath.parse(path));
    }

    public <T> ConfigKey<T> registerDefinition(ConfigDefinition<T> definition) {
        if (definitions.containsKey(definition.path())) {
            throw new ConfigException(
                    "Duplicate config definition: " + definition.path().fullPath());
        }
        definitions.put(definition.path(), definition);
        return new ConfigKey<>(id, definition);
    }

    public ConfigValue get(String path) {
        ConfigPath configPath = ConfigPath.parse(path);
        JsonObject document = document(configPath.file());
        JsonElement element = JsonPaths.get(document, configPath);
        return new ConfigValue(this, configPath, element);
    }

    public <T> T get(ConfigKey<T> key) {
        assertOwns(key);
        JsonObject document = document(key.path().file());
        JsonElement element = JsonPaths.get(document, key.path());
        if (element == null || element.isJsonNull()) {
            return key.definition().defaultValue();
        }
        T value = ConfigValues.fromJson(element, key.definition().type());
        ValidationResult result = key.definition().validate(value, this);
        if (!result.valid()) {
            throw new ConfigValidationException(
                    "Invalid config value at " + key.path().fullPath() + ": " + result.message());
        }
        return value;
    }

    public ConfigMutation set(String path, Object value) {
        ConfigPath configPath = ConfigPath.parse(path);
        return setRaw(configPath, value);
    }

    public <T> ConfigMutation set(ConfigKey<T> key, T value) {
        assertOwns(key);
        ValidationResult result = key.definition().validate(value, this);
        if (!result.valid()) {
            throw new ConfigValidationException(
                    "Invalid config value at " + key.path().fullPath() + ": " + result.message());
        }
        return setRaw(key.path(), value);
    }

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

    public void reload() {
        if (!dirtyFiles.isEmpty()) {
            throw new ConfigException("Cannot reload config '" + id + "' because unsaved changes exist: " + dirtyFiles
                    + ". Call save() or discardAndReload().");
        }
        load();
    }

    public void discardAndReload() {
        documents.clear();
        dirtyFiles.clear();
        load();
    }

    public void save() {
        for (String file : List.copyOf(dirtyFiles)) {
            save(file);
        }
    }

    public void save(String file) {
        JsonObject document = documents.get(file);
        if (document == null) {
            return;
        }
        storage.save(file, document);
        dirtyFiles.remove(file);
    }

    public boolean isDirty() {
        return !dirtyFiles.isEmpty();
    }

    public Set<String> dirtyFiles() {
        return Set.copyOf(dirtyFiles);
    }

    public void registerSanitizeHook(Runnable hook) {
        sanitizeHooks.add(hook);
    }

    public <T extends Number & Comparable<T>> void repairMinMax(ConfigKey<T> minKey, ConfigKey<T> maxKey) {
        T min = get(minKey);
        T max = get(maxKey);
        if (min.compareTo(max) <= 0) {
            return;
        }
        set(minKey, max);
    }

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
}

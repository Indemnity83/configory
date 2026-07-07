# Custom Storage Backends

By default a `Config` reads and writes a JSON file under `config/` in the Minecraft config
directory, named by its id (`config/examplemod.json`; a dotted id nests into subdirectories). That
behavior lives behind the `ConfigStorage` interface, so you can swap in a different backend.

## The `ConfigStorage` interface

`ConfigStorage` is a small service-provider interface: it maps a config id to its JSON document.

```java
public interface ConfigStorage {
    JsonObject load(String file);
    void save(String file, JsonObject root);
}
```

- `load(file)` returns the stored document for a config id, or a new empty `JsonObject` if it
  doesn't exist. It throws `ConfigException` if the file exists but can't be read or parsed. (The
  parameter is named `file`, but it is the config id; `JsonFileConfigStorage` maps a dotted id to a
  nested `.json` path.)
- `save(file, root)` persists a document, replacing any prior contents; it throws
  `ConfigException` if the document can't be written.

The default implementation is `JsonFileConfigStorage`, which reads and writes `<file>.json` on
disk.

## Supplying your own

Pass a storage implementation to `Config.create(id, storage)`:

```java
ConfigStorage storage = new MyDatabaseConfigStorage();
Config config = Config.create("examplemod", storage);
```

Everything else — definitions, validation, dirty tracking, save/reload — works unchanged; only
where the bytes land is different.

## With the registry and bootstrap conventions

`Config.create(id, storage)` builds a config directly, but the
[bootstrap convention](digging-deeper/bootstrap-convention.md) and `ConfigRegistry` normally create
a **file-backed** config the first time an id is looked up. To inject storage through that path —
for tests, or when a loader supplies the config directory — use the storage overloads:

```java
// registry
Config config = ConfigRegistry.getOrCreate("examplemod", storage);

// ConfigHost mix-in
bootstrapConfig("examplemod", storage);
Config config = config("examplemod", storage);

// inside a Configs holder (ConfigEntries)
private static final Config config = configFor("examplemod", storage);
```

The storage is applied only when the config is **first** created for that id; a config already
registered for the id is returned unchanged. Register the storage before anything else touches the
id — for example in a test's setup, before the `Configs` holder class initializes.

> [!NOTE]
> Injected storage applies to that one config. Child configs declared with `configFor(modId, name)`
> still use the default file store; to back a child with custom storage, create it directly with
> `Config.create(id, storage)`.

## In-memory storage for tests

The most common custom backend is an in-memory store, so tests never touch the real filesystem.
A minimal implementation:

```java
public final class InMemoryConfigStorage implements ConfigStorage {
    private final Map<String, JsonObject> files = new HashMap<>();

    @Override
    public JsonObject load(String file) {
        JsonObject stored = files.get(file);
        return stored == null ? new JsonObject() : stored.deepCopy();
    }

    @Override
    public void save(String file, JsonObject root) {
        files.put(file, root.deepCopy());
    }
}
```

Then build a config around it:

```java
Config config = Config.create("examplemod", new InMemoryConfigStorage());
config.defineFloat("core.speed_multiplier", 1.0f).register();
config.load();
```

> [!TIP]
> Configory's own test suite uses exactly this pattern — an in-memory `ConfigStorage` double — so
> unit tests run without disk I/O. Copy `deepCopy()` on both load and save so callers can't mutate
> your stored documents by reference.

## Next steps

- [Error Handling](digging-deeper/error-handling.md) — the exceptions storage can raise.
- [Manual Access](digging-deeper/manual-access.md) — driving a config without `ConfigHost`.

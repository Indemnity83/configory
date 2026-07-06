# Custom Storage Backends

By default a `Config` reads and writes JSON files under `config/<id>/` in the Minecraft config
directory. That behavior lives behind the `ConfigStorage` interface, so you can swap in a
different backend.

## The `ConfigStorage` interface

`ConfigStorage` is a small service-provider interface: it maps file names to JSON documents.

```java
public interface ConfigStorage {
    JsonObject load(String file);
    void save(String file, JsonObject root);
}
```

- `load(file)` returns the stored document for a file name (without the `.json` extension), or a
  new empty `JsonObject` if it doesn't exist. It throws `ConfigException` if the file exists but
  can't be read or parsed.
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
config.define("core.speed_multiplier").asFloat().defaultValue(1.0f).register();
config.load();
```

> [!TIP]
> Configory's own test suite uses exactly this pattern — an in-memory `ConfigStorage` double — so
> unit tests run without disk I/O. Copy `deepCopy()` on both load and save so callers can't mutate
> your stored documents by reference.

## Next steps

- [Error Handling](digging-deeper/error-handling.md) — the exceptions storage can raise.
- [Manual Access](digging-deeper/manual-access.md) — driving a config without `ConfigHost`.

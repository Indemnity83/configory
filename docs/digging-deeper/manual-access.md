# Manual Access

You can use Configory without `ConfigHost` and without the bootstrap convention. This suits tests,
tools, and non-standard mod layouts where the nested-`Configs` pattern doesn't fit.

```java
Config config = ConfigRegistry.config("examplemod");

ConfigKey<Float> speedMultiplier =
        config.define("core.speed_multiplier")
                .asFloat()
                .defaultValue(1.0f)
                .range(0.1f, 10.0f)
                .register();

config.load();

float speed = config.get(speedMultiplier);
config.set(speedMultiplier, 3.0f).save();
```

The only extra responsibility compared to the bootstrap flow is calling `config.load()` yourself
once your keys are registered — that's the step `bootstrapConfig(...)` would otherwise handle.

## Getting a `Config`

`ConfigRegistry` maps each id to a single shared `Config` instance, so every part of your code
sees the same config:

```java
Config config = ConfigRegistry.config("examplemod");
```

`ConfigRegistry.getOrCreate("examplemod")` is a synonym for `config(...)` — both return the shared
instance, creating a file-backed one on first access.

> [!NOTE]
> Because the registry is process-wide and keyed by id, you don't need to pass a `Config` around.
> Any code that knows the mod id can call `ConfigRegistry.config(id)` and get the same instance
> your keys were registered against.

## Constructing a `Config` directly

For full control — most often in tests — construct a `Config` without the registry:

```java
Config config = Config.create("examplemod");                 // file-backed
Config test = Config.create("examplemod", inMemoryStorage);  // custom storage
```

The second form takes a `ConfigStorage`, which is how you back a config with something other than
JSON files on disk. See [Custom Storage Backends](digging-deeper/custom-storage.md).

## Next steps

- [Custom Storage Backends](digging-deeper/custom-storage.md) — swap the persistence layer.
- [Error Handling](digging-deeper/error-handling.md) — what the manual calls can throw.

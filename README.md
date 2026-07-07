# Configory

**Convention-based configuration for Minecraft mods.**

Configory is a small Java configuration library for Minecraft mods that combines:

- dot-notation paths that nest into one JSON file per config
- typed config keys
- fluent validation
- per-mod config isolation
- JSON-backed config files
- runtime mutation with explicit saving
- lightweight bootstrap conventions

It is designed to make configuration feel simple at the call site without giving up type safety
where mod code needs it.

```java
float speed = getConfig(Configs.SPEED_MULTIPLIER);

setConfig(Configs.SPEED_MULTIPLIER, 3.0f).save();
```

Or, when you need dynamic path access:

```java
float speed = getConfig("core.speed_multiplier").asFloat();

setConfig("core.speed_multiplier", 3.0f).save();
```

## Why Configory?

Minecraft mod configuration often ends up being a mix of boilerplate, static constants, validation
code, config file handling, command integration, and awkward call sites.

Configory aims to make the common case pleasant. Define a value once, with its type, default, and
constraints:

```java
public static final ConfigKey<Float> SPEED_MULTIPLIER =
        config.defineFloat("core.speed_multiplier", 1.0f)
                .range(0.1f, 10.0f)
                .describe("Global speed multiplier.")
                .register();
```

Then use it safely from mod code:

```java
float speed = getConfig(Configs.SPEED_MULTIPLIER);
```

The string path is still there whenever you want terminal commands, debugging, scripts, or dynamic
tools:

```java
float speed = getConfig("core.speed_multiplier").asFloat();
```

## Core concepts

- **The config id is the file.** A simple mod is one `config/<modid>.json`; mods never collide
  because each is scoped to its own id.
- **Dot-notation paths are pure nesting.** `"engines.stirling.min_output"` nests
  `engines → stirling → min_output` inside the config's file — no hidden "first segment is a file"
  rule.
- **Multiple files when you want them.** Declare extra configs with `configFor(MOD_ID, "engines")`
  (→ `config/<modid>/engines.json`); `bootstrapConfig(MOD_ID)` loads the whole tree.
- **Typed keys and string paths.** `ConfigKey<T>` constants give Java type safety for mod code;
  string paths give flexibility for commands and tools.
- **Explicit save and reload.** Runtime `set` changes memory only; nothing hits disk until you
  `save()`, and reload refuses to discard unsaved changes.

## Quick start

Implement `ConfigHost`, keep your keys in a nested `Configs` class, and call
`bootstrapConfig(MOD_ID)` once during init:

```java
public final class ExampleMod implements ConfigHost {
    public static final String MOD_ID = "examplemod";

    public void initCommon() {
        bootstrapConfig(MOD_ID);
    }

    public static final class Configs extends ConfigEntries {
        private static final Config config = configFor(MOD_ID);

        private Configs() {}

        public static final ConfigKey<Float> SPEED_MULTIPLIER =
                config.defineFloat("core.speed_multiplier", 1.0f)
                        .range(0.1f, 10.0f)
                        .describe("Global speed multiplier.")
                        .register();
    }
}
```

That is enough to define, load, validate, and use config values. Read and write them anywhere:

```java
float speed = getConfig(Configs.SPEED_MULTIPLIER);

setConfig(Configs.SPEED_MULTIPLIER, 3.0f).save();
```

## Documentation

Full documentation lives at **[indemnity83.github.io/configory](https://indemnity83.github.io/configory/)**:

- **Getting Started** — installation, quick start, and recommended mod structure.
- **The Basics** — configuration paths, defining values, reading, writing &amp; saving, reloading.
- **Digging Deeper** — validation, cross-value rules, introspection, the bootstrap convention,
  manual access, custom storage backends, error handling, and generated files.
- **Reference** — API summary and design goals.

## License

Configory is licensed under the MIT License.

Minecraft is a trademark of Microsoft. Configory is not affiliated with or endorsed by Mojang
Studios or Microsoft.

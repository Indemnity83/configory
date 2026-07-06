# Configory

**Convention-based configuration for Minecraft mods.**

Configory is a small Java configuration library for Minecraft mods that combines:

- dot-notation paths (or a single default file for simple mods)
- typed config keys
- fluent validation
- per-mod config isolation
- JSON-backed config files
- runtime mutation with explicit saving
- lightweight bootstrap conventions

It is designed to make configuration feel simple at the call site without giving up type safety where mod code needs it.

```java
float speed = getConfig(Configs.SPEED_MULTIPLIER);

setConfig(Configs.SPEED_MULTIPLIER, 3.0f).save();
```

Or, when you need dynamic path access:

```java
float speed = getConfig("core.speed_multiplier").asFloat();

setConfig("core.speed_multiplier", 3.0f).save();
```

---

## Why Configory?

Minecraft mod configuration often ends up being a mix of boilerplate, static constants, validation code, config file handling, command integration, and awkward call sites.

Configory aims to make the common case pleasant:

```java
public static final ConfigKey<Float> SPEED_MULTIPLIER =
        config.define("core.speed_multiplier")
                .asFloat()
                .defaultValue(1.0f)
                .range(0.1f, 10.0f)
                .describe("Global speed multiplier.")
                .register();
```

Then use it safely elsewhere:

```java
float speed = getConfig(Configs.SPEED_MULTIPLIER);
```

The string path is still available when you want terminal commands, debugging, scripts, or dynamic tools:

```java
float speed = getConfig("core.speed_multiplier").asFloat();
```

---

## Core concepts

Configory is built around a few simple ideas.

### One config instance per mod

Each consuming mod gets its own isolated config namespace.

```text
.minecraft/config/logistics/core.json
.minecraft/config/logistics/engines.json
.minecraft/config/other_mod/core.json
```

The mod id becomes the folder name under the Minecraft config directory.

### Dot notation paths

The first path segment is the config file. Remaining segments traverse JSON.

```java
"core.speed_multiplier"
```

maps to:

```text
config/<modid>/core.json
```

```json
{
  "speed_multiplier": 1.0
}
```

Nested paths are supported:

```java
"engines.stirling.min_output"
```

maps to:

```text
config/<modid>/engines.json
```

```json
{
  "stirling": {
    "min_output": 3.0
  }
}
```

### Single-file configs

Not every mod wants multiple files. A path with **no dot** is a bare key, and lands in the default `config.json`:

```java
"speed_multiplier"
```

maps to:

```text
config/<modid>/config.json
```

```json
{
  "speed_multiplier": 1.0
}
```

`config` is the reserved name of that default file. Writing it out explicitly is allowed and is just an alias — these two paths are identical:

```java
"speed_multiplier"          // bare key
"config.speed_multiplier"   // explicit alias — same file, same key
```

Paths always render in qualified form (`config.speed_multiplier`) in error messages and logs, so the backing file is never ambiguous. You can mix styles freely: bare keys share `config.json` while dotted keys split into their own files.

### Typed keys for mod code

Use `ConfigKey<T>` constants when writing normal mod code.

```java
public static final ConfigKey<Double> STIRLING_MIN_OUTPUT =
        config.define("engines.stirling.min_output")
                .asDouble()
                .defaultValue(3.0)
                .min(0.0)
                .describe("Stirling engine minimum RF/t output.")
                .register();
```

Then:

```java
double minOutput = getConfig(Configs.STIRLING_MIN_OUTPUT);
```

### Dynamic paths for tools

String paths are still supported:

```java
double minOutput = getConfig("engines.stirling.min_output").asDouble();
```

This is useful for commands, terminals, debug menus, generated docs, and config screens.

---

## Quick start

### 1. Implement `ConfigHost`

Your mod entry point implements `ConfigHost`.

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
                config.define("core.speed_multiplier")
                        .asFloat()
                        .defaultValue(1.0f)
                        .range(0.1f, 10.0f)
                        .describe("Global speed multiplier.")
                        .register();
    }
}
```

That is enough to define, load, validate, and use config values.

### 2. Read config values

From inside your mod entry point or another class with access to the host helper methods:

```java
float speed = getConfig(Configs.SPEED_MULTIPLIER);
```

From static code:

```java
float speed = ConfigRegistry.config(ExampleMod.MOD_ID)
        .get(ExampleMod.Configs.SPEED_MULTIPLIER);
```

Or use a path directly:

```java
float speed = ConfigRegistry.config(ExampleMod.MOD_ID)
        .get("core.speed_multiplier")
        .asFloat();
```

### 3. Set config values

`set` changes the runtime value only.

```java
setConfig(Configs.SPEED_MULTIPLIER, 3.0f);
```

To save that changed file immediately:

```java
setConfig(Configs.SPEED_MULTIPLIER, 3.0f).save();
```

To save all dirty config files:

```java
saveConfig();
```

Or directly:

```java
ConfigRegistry.config(ExampleMod.MOD_ID).save();
```

---

## Recommended mod structure

For most mods, place config definitions in a nested `Configs` class inside your mod entry point.

```java
public final class LogisticsMod implements ConfigHost {
    public static final String MOD_ID = "logistics";

    public void initCommon() {
        bootstrapConfig(MOD_ID);
    }

    public static final class Configs extends ConfigEntries {
        private static final Config config = configFor(MOD_ID);

        private Configs() {}

        public static final ConfigKey<Float> SPEED_MULTIPLIER =
                config.define("core.speed_multiplier")
                        .asFloat()
                        .defaultValue(1.0f)
                        .range(0.1f, 10.0f)
                        .describe("Global speed multiplier.")
                        .register();

        public static final ConfigKey<Long> REDSTONE_OUTPUT =
                config.define("engines.redstone.output")
                        .asLong()
                        .defaultValue(10L)
                        .min(0L)
                        .describe("RF generated per 16-tick interval.")
                        .register();

        public static final ConfigKey<Double> STIRLING_MIN_OUTPUT =
                config.define("engines.stirling.min_output")
                        .asDouble()
                        .defaultValue(3.0)
                        .min(0.0)
                        .maxValueOf(() -> STIRLING_MAX_OUTPUT)
                        .describe("Stirling engine minimum RF/t output.")
                        .register();

        public static final ConfigKey<Double> STIRLING_MAX_OUTPUT =
                config.define("engines.stirling.max_output")
                        .asDouble()
                        .defaultValue(10.0)
                        .min(0.0)
                        .minValueOf(() -> STIRLING_MIN_OUTPUT)
                        .describe("Stirling engine maximum RF/t output.")
                        .register();

        public static void bootstrap(Config config) {
            config.registerSanitizeHook(() ->
                    config.repairMinMax(STIRLING_MIN_OUTPUT, STIRLING_MAX_OUTPUT)
            );
        }
    }
}
```

This gives you:

```java
double minOutput = getConfig(Configs.STIRLING_MIN_OUTPUT);
setConfig(Configs.STIRLING_MAX_OUTPUT, 12.0).save();
```

---

## Defining values

Configory supports common primitive types.

### Float

```java
public static final ConfigKey<Float> SPEED_MULTIPLIER =
        config.define("core.speed_multiplier")
                .asFloat()
                .defaultValue(1.0f)
                .range(0.1f, 10.0f)
                .describe("Global speed multiplier.")
                .register();
```

### Double

```java
public static final ConfigKey<Double> EFFICIENCY =
        config.define("machines.efficiency")
                .asDouble()
                .defaultValue(0.85)
                .range(0.0, 1.0)
                .describe("Machine efficiency multiplier.")
                .register();
```

### Integer

```java
public static final ConfigKey<Integer> MAX_AREA =
        config.define("machines.quarry.max_area")
                .asInt()
                .defaultValue(64)
                .min(1)
                .max(256)
                .describe("Maximum quarry area.")
                .register();
```

### Long

```java
public static final ConfigKey<Long> ENERGY_CAPACITY =
        config.define("machines.battery.capacity")
                .asLong()
                .defaultValue(100_000L)
                .min(0L)
                .describe("Battery energy capacity.")
                .register();
```

### Boolean

```java
public static final ConfigKey<Boolean> ENABLE_DEBUG =
        config.define("core.debug")
                .asBoolean()
                .defaultValue(false)
                .describe("Enables debug logging.")
                .register();
```

### String

```java
public static final ConfigKey<String> ENERGY_UNIT =
        config.define("core.energy_unit")
                .asString()
                .defaultValue("FE")
                .allowedValues("FE", "RF", "MJ")
                .describe("Displayed energy unit label.")
                .register();
```

---

## Reading values

### Typed key access

Preferred for normal mod code:

```java
float speed = getConfig(Configs.SPEED_MULTIPLIER);
int maxArea = getConfig(Configs.MAX_AREA);
boolean debug = getConfig(Configs.ENABLE_DEBUG);
```

Typed key access gives Java static analysis and avoids string typos.

### Path access

Useful for commands, terminals, debug tools, generated screens, or dynamic lookups:

```java
float speed = getConfig("core.speed_multiplier").asFloat();
int maxArea = getConfig("machines.quarry.max_area").asInt();
boolean debug = getConfig("core.debug").asBoolean();
```

Path access also supports fallbacks:

```java
float speed = getConfig("core.speed_multiplier").asFloat(1.0f);
int maxArea = getConfig("machines.quarry.max_area").asInt(64);
boolean debug = getConfig("core.debug").asBoolean(false);
```

---

## Writing values

### Runtime-only mutation

`set` updates the runtime value and marks the backing file dirty.

```java
setConfig(Configs.SPEED_MULTIPLIER, 3.0f);
```

This does not immediately write to disk.

### Runtime mutation and save

To set and save the changed file:

```java
setConfig(Configs.SPEED_MULTIPLIER, 3.0f).save();
```

The chained `.save()` saves only the file affected by that mutation.

### Save all dirty files

```java
saveConfig();
```

Or:

```java
ConfigRegistry.config(MOD_ID).save();
```

### Save one file

```java
ConfigRegistry.config(MOD_ID).save("core");
```

---

## Reloading

Reloading reads values from disk again.

```java
reloadConfig();
```

By default, reload is protected from accidental data loss. If there are unsaved runtime changes, `reloadConfig()` throws.

To explicitly discard unsaved runtime changes and reload from disk:

```java
ConfigRegistry.config(MOD_ID).discardAndReload();
```

---

## Validation

Validation is declared fluently during registration.

```java
public static final ConfigKey<Integer> MAX_AREA =
        config.define("machines.quarry.max_area")
                .asInt()
                .defaultValue(64)
                .range(1, 256)
                .describe("Maximum quarry area.")
                .register();
```

Validation runs when:

- config files are loaded
- defaults are applied
- values are set at runtime
- sanitize hooks repair related values

Invalid values loaded from disk are replaced with defaults and marked dirty so they can be saved back cleanly.

---

## Cross-value validation

Some values depend on other config values.

```java
public static final ConfigKey<Double> STIRLING_MIN_OUTPUT =
        config.define("engines.stirling.min_output")
                .asDouble()
                .defaultValue(3.0)
                .min(0.0)
                .maxValueOf(() -> STIRLING_MAX_OUTPUT)
                .describe("Stirling engine minimum RF/t output.")
                .register();

public static final ConfigKey<Double> STIRLING_MAX_OUTPUT =
        config.define("engines.stirling.max_output")
                .asDouble()
                .defaultValue(10.0)
                .min(0.0)
                .minValueOf(() -> STIRLING_MIN_OUTPUT)
                .describe("Stirling engine maximum RF/t output.")
                .register();
```

Suppliers are used so static initialization order does not break cross-references.

For repair behavior, register a sanitize hook:

```java
public static void bootstrap(Config config) {
    config.registerSanitizeHook(() ->
            config.repairMinMax(STIRLING_MIN_OUTPUT, STIRLING_MAX_OUTPUT)
    );
}
```

---

## Descriptions

Every config key can include a description:

```java
.describe("RF generated per 16-tick interval.")
```

Descriptions are intended for:

- terminal commands
- generated config documentation
- future config screens
- schema generation
- tooltips
- help output

Configory uses `.describe(...)` instead of `.comment(...)` because descriptions are not limited to file comments.

---

## Generated files

Given this config definition:

```java
public static final ConfigKey<Float> SPEED_MULTIPLIER =
        config.define("core.speed_multiplier")
                .asFloat()
                .defaultValue(1.0f)
                .range(0.1f, 10.0f)
                .describe("Global speed multiplier.")
                .register();
```

Configory writes:

```text
.minecraft/config/examplemod/core.json
```

```json
{
  "speed_multiplier": 1.0
}
```

Given this definition:

```java
public static final ConfigKey<Double> STIRLING_MIN_OUTPUT =
        config.define("engines.stirling.min_output")
                .asDouble()
                .defaultValue(3.0)
                .min(0.0)
                .describe("Stirling engine minimum RF/t output.")
                .register();
```

Configory writes:

```text
.minecraft/config/examplemod/engines.json
```

```json
{
  "stirling": {
    "min_output": 3.0
  }
}
```

For a simple mod that only uses bare keys:

```java
public static final ConfigKey<Float> SPEED_MULTIPLIER =
        config.define("speed_multiplier")
                .asFloat()
                .defaultValue(1.0f)
                .range(0.1f, 10.0f)
                .register();
```

Configory writes a single default file:

```text
.minecraft/config/examplemod/config.json
```

```json
{
  "speed_multiplier": 1.0
}
```

---

## Bootstrap convention

Configory uses a simple convention:

> Put your config keys in a nested class named `Configs`, then call `bootstrapConfig(modId)`.

```java
public final class ExampleMod implements ConfigHost {
    public static final String MOD_ID = "examplemod";

    public void initCommon() {
        bootstrapConfig(MOD_ID);
    }

    public static final class Configs extends ConfigEntries {
        private static final Config config = configFor(MOD_ID);

        public static final ConfigKey<Float> SPEED_MULTIPLIER =
                config.define("core.speed_multiplier")
                        .asFloat()
                        .defaultValue(1.0f)
                        .register();
    }
}
```

`bootstrapConfig(modId)` handles:

1. locating the nested `Configs` class
2. forcing static config keys to initialize
3. running optional `Configs.bootstrap(Config config)`
4. loading config files
5. applying defaults
6. validating values
7. running sanitize hooks

---

## Optional bootstrap hook

Add a static `bootstrap(Config config)` method inside your `Configs` class when you need setup that depends on all keys already existing.

```java
public static final class Configs extends ConfigEntries {
    private static final Config config = configFor(MOD_ID);

    public static final ConfigKey<Double> MIN =
            config.define("machines.min")
                    .asDouble()
                    .defaultValue(3.0)
                    .register();

    public static final ConfigKey<Double> MAX =
            config.define("machines.max")
                    .asDouble()
                    .defaultValue(10.0)
                    .register();

    public static void bootstrap(Config config) {
        config.registerSanitizeHook(() -> config.repairMinMax(MIN, MAX));
    }
}
```

---

## Advanced: manual config access

You can use Configory without `ConfigHost`.

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

This is useful for tests, tools, or non-standard mod layouts.

---

## API summary

### Config host

```java
bootstrapConfig(modId);
getConfig(path);
getConfig(key);
setConfig(path, value);
setConfig(key, value);
saveConfig();
reloadConfig();
```

### Config instance

```java
config.define(path);
config.get(path);
config.get(key);
config.set(path, value);
config.set(key, value);
config.save();
config.save(file);
config.reload();
config.discardAndReload();
```

### Reads

```java
config.get("core.speed_multiplier").asFloat();
config.get("core.speed_multiplier").asFloat(1.0f);

config.get(Configs.SPEED_MULTIPLIER);
```

### Writes

```java
config.set("core.speed_multiplier", 3.0f);
config.set("core.speed_multiplier", 3.0f).save();

config.set(Configs.SPEED_MULTIPLIER, 3.0f);
config.set(Configs.SPEED_MULTIPLIER, 3.0f).save();
```

### Definitions

```java
config.define("core.speed_multiplier")
        .asFloat()
        .defaultValue(1.0f)
        .range(0.1f, 10.0f)
        .describe("Global speed multiplier.")
        .register();
```

---

## Design goals

Configory is intentionally small.

It is not trying to be a full settings UI, command framework, or complete config ecosystem. It is the config foundation those things can build on.

The goals are:

- simple public API
- predictable file layout
- low boilerplate
- typed keys where Java code needs safety
- string paths where tools need flexibility
- explicit save/reload behavior
- validation near the definition
- mod-level isolation by default

---

## Roadmap ideas

Possible future additions:

- `trySet(...)` for command and GUI validation
- generated Markdown docs
- generated JSON schema
- config command helpers
- config screen metadata
- change listeners with `.onChange(...)`
- client/server sync metadata
- restart-required flags
- JSON5/TOML storage backends
- list and object config values
- migration helpers for renamed keys

---

## License

Configory is licensed under the MIT License.

Minecraft is a trademark of Microsoft. Configory is not affiliated with or endorsed by Mojang Studios or Microsoft.

# API Summary

A quick reference to the surface you'll use most. For the full behavior of each method, follow
the links into the guides.

## Config host helpers

Available on any class that implements [`ConfigHost`](getting-started/quick-start.md):

```java
bootstrapConfig(modId);   // wire up config for an explicit id
bootstrapConfig();        // resolve the id from MOD_ID / MODID
getConfig(path);          // -> ConfigValue (dynamic read)
getConfig(key);           // -> T (typed read)
setConfig(path, value);   // -> ConfigMutation (dynamic write, no validation)
setConfig(key, value);    // -> ConfigMutation (typed write, validated)
saveConfig();             // save all dirty files
reloadConfig();           // safe reload (throws on unsaved changes)
```

## Config instance

Obtained from `ConfigRegistry.config(id)` or `Config.create(id)`:

```java
config.defineFloat(path, def);  // typed definition shorthand (also Boolean/String/Int/Long/Double)
config.define(path);            // start a definition -> builder (long form: .asX())
config.get(path);               // -> ConfigValue
config.get(key);                // -> T
config.set(path, value);        // -> ConfigMutation (no validation)
config.set(key, value);         // -> ConfigMutation (validated)
config.trySet(path, value);     // -> boolean (validates if a definition exists; no throw)
config.trySet(key, value);      // -> boolean (validated; no throw)
config.save();                  // save all dirty files
config.save(file);              // save one file
config.reload();                // safe reload
config.discardAndReload();      // reload, discarding unsaved changes
config.isDirty();               // any unsaved changes?
config.dirtyFiles();            // snapshot of dirty file names
config.registerSanitizeHook(r); // run r after each load
config.repairMinMax(min, max);  // order a min/max pair (for hooks)
config.definitions();           // all registered definitions (for tooling)
```

## Registry

```java
ConfigRegistry.config(id);       // shared Config for id (creates on first access)
ConfigRegistry.getOrCreate(id);  // synonym for config(id)
```

## Reads

```java
config.get("core.speed_multiplier").asFloat();        // strict — throws on absent/invalid
config.get("core.speed_multiplier").asFloat(1.0f);    // fallback — 1.0f on absent/invalid

config.get(Configs.SPEED_MULTIPLIER);                 // typed — default when unset
```

See [Reading Values](the-basics/reading-values.md).

## Writes

```java
config.set("core.speed_multiplier", 3.0f);            // runtime only, no validation
config.set("core.speed_multiplier", 3.0f).save();     // and persist the affected file

config.set(Configs.SPEED_MULTIPLIER, 3.0f);           // runtime only, validated
config.set(Configs.SPEED_MULTIPLIER, 3.0f).save();    // and persist

if (config.trySet("core.speed_multiplier", input)) {  // validate untrusted input, no throw
    config.save();
}
```

See [Writing &amp; Saving](the-basics/writing-and-saving.md).

## Definitions

```java
config.defineFloat("core.speed_multiplier", 1.0f)  // shorthand: type + default
        .range(0.1f, 10.0f)              // min / max / range / minValueOf / maxValueOf
        .describe("Global speed multiplier.")
        .alias("core.old_speed")         // migrate a value from a former path (repeatable)
        .validator((v, c) -> /* ... */)  // optional custom rule
        .register();                     // -> ConfigKey<Float>
```

The long form spells the type and default as separate steps:

```java
config.define("core.speed_multiplier")
        .asFloat()                       // asBoolean/asString/asInt/asLong/asFloat/asDouble
        .defaultValue(1.0f)              // or defaultsTo(1.0f)
        .range(0.1f, 10.0f)
        .register();
```

See [Defining Values](the-basics/defining-values.md) and [Validation](digging-deeper/validation.md).

## Exceptions

- `ConfigException` — general errors (missing `Configs`, duplicate definition, failed strict read,
  reload with unsaved changes, storage failure, …).
- `ConfigValidationException` (extends `ConfigException`) — a value failed validation.

See [Error Handling](digging-deeper/error-handling.md).

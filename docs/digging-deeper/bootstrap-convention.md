# The Bootstrap Convention

Configory uses one small convention to wire a mod's config together:

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
                config.defineFloat("core.speed_multiplier", 1.0f).register();
    }
}
```

## What `bootstrapConfig(modId)` does

A single call performs, in order:

1. **Locate** the nested class named `Configs` on the host.
2. **Force-initialize** it, which runs the `static final` field initializers and registers every
   key with the config.
3. **Run** the optional `Configs.bootstrap(Config)` hook, if present.
4. **Load** the config tree — the main config's file **and** every child config declared with
   `configFor(modId, "<name>")` (each at `config/<modid>/<name>.json`): reading each file, applying
   defaults for missing keys, validating stored values (replacing invalid ones with defaults and
   marking the config dirty), and finally running any registered sanitize hooks.

After it returns, every value is defined, loaded, validated, and ready to read.

## Resolving the id automatically

`bootstrapConfig(modId)` takes the id explicitly, but there's also a no-arg form that discovers
it from the host class:

```java
public void initCommon() {
    bootstrapConfig();   // resolves the id from MOD_ID / MODID
}
```

The no-arg `bootstrapConfig()` looks for a `public static final String` field named `MOD_ID`,
then `MODID`, on the host class. It throws a `ConfigException` if neither exists (or the value is
blank) — in that case pass the id explicitly.

## The id is the file location

A config's id *is* where its file lives: `configFor("examplemod")` → `config/examplemod.json`, and a
dotted id nests into subdirectories. The idiomatic way to add a file is `configFor(MOD_ID, "engines")`
(→ `config/examplemod/engines.json`), which `bootstrapConfig(MOD_ID)` loads automatically alongside
the main config. You can also address any id directly — nothing requires it to match your mod id:

```java
Config shared = ConfigRegistry.getOrCreate("some_shared_namespace");  // config/some_shared_namespace.json
```

Call sites that share an id share one `Config` (and one file); distinct ids are fully independent.
Keep ids unique and filesystem-safe — your mod id is convenient precisely because it already is.

## The optional bootstrap hook

Add a static `bootstrap(Config config)` method inside your `Configs` class when you need setup
that depends on all keys already existing — most commonly registering a sanitize hook for
cross-field repair:

```java
public static final class Configs extends ConfigEntries {
    private static final Config config = configFor(MOD_ID);

    public static final ConfigKey<Double> MIN =
            config.defineDouble("machines.min", 3.0).register();

    public static final ConfigKey<Double> MAX =
            config.defineDouble("machines.max", 10.0).register();

    public static void bootstrap(Config config) {
        config.registerSanitizeHook(() -> config.repairMinMax(MIN, MAX));
    }
}
```

The hook runs at step 3 above — after every key is registered, but before the files are loaded —
so anything it registers is in place by the time `load()` validates and sanitizes. See
[Cross-Value Validation](digging-deeper/cross-value-validation.md).

## When you can't follow the convention

If your mod can't use the nested-`Configs` layout, skip `ConfigHost` entirely and drive the
config yourself. See [Manual Access](digging-deeper/manual-access.md).

## Next steps

- [Manual Access](digging-deeper/manual-access.md) — bootstrap-free usage.
- [Cross-Value Validation](digging-deeper/cross-value-validation.md) — the main use for the hook.

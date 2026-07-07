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
4. **Load** the config files — reading each file, applying defaults for missing keys, validating
   stored values (replacing invalid ones with defaults and marking those files dirty), and finally
   running any registered sanitize hooks.

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

## Advanced: using an id other than your mod id

The id is just a string: it names the config folder (`config/<id>/`) and is the key the registry
shares instances under. Matching your mod id is the convention — and what the no-arg forms
auto-resolve — but nothing requires it. For full control, pass any id to the explicit forms:

```java
bootstrapConfig("examplemod_client");                        // ConfigHost
Config config = configFor("examplemod_client");              // inside a Configs holder
Config config = ConfigRegistry.getOrCreate("examplemod_client");
```

Because the id is the registry key, call sites that share an id share one `Config` (and one set of
files), while distinct ids are fully independent — so you can split a mod's settings across several
configs by giving each its own id. Keep ids unique and filesystem-safe; your mod id is convenient
precisely because it already is.

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

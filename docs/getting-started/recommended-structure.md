# Recommended Mod Structure

For most mods, place config definitions in a nested `Configs` class inside your mod entry point.
This keeps every key in one discoverable place, lets `bootstrapConfig(...)` find them by
convention, and gives you typed constants to reference from mod code.

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
                config.defineFloat("core.speed_multiplier", 1.0f)
                        .range(0.1f, 10.0f)
                        .describe("Global speed multiplier.")
                        .register();

        public static final ConfigKey<Long> REDSTONE_OUTPUT =
                config.defineLong("engines.redstone.output", 10L)
                        .min(0L)
                        .describe("RF generated per 16-tick interval.")
                        .register();

        public static final ConfigKey<Double> STIRLING_MIN_OUTPUT =
                config.defineDouble("engines.stirling.min_output", 3.0)
                        .min(0.0)
                        .maxValueOf(() -> Configs.STIRLING_MAX_OUTPUT)
                        .describe("Stirling engine minimum RF/t output.")
                        .register();

        public static final ConfigKey<Double> STIRLING_MAX_OUTPUT =
                config.defineDouble("engines.stirling.max_output", 10.0)
                        .min(0.0)
                        .minValueOf(() -> STIRLING_MIN_OUTPUT)
                        .describe("Stirling engine maximum RF/t output.")
                        .register();

        public static void bootstrap(Config config) {
            config.registerSanitizeHook(() ->
                    config.repairMinMax(STIRLING_MIN_OUTPUT, STIRLING_MAX_OUTPUT));
        }
    }
}
```

This gives you clean, typed access anywhere in your mod:

```java
double minOutput = getConfig(Configs.STIRLING_MIN_OUTPUT);
setConfig(Configs.STIRLING_MAX_OUTPUT, 12.0).save();
```

## Why this layout

- **`Configs extends ConfigEntries`** — the base class provides `configFor(modId)`, and the
  nested-class name `Configs` is what `bootstrapConfig(...)` looks for by convention.
- **`public static final` keys** — forcing the class to initialize registers every key exactly
  once; the constants double as your typed access points.
- **A private constructor** — the holder is never instantiated.
- **An optional `bootstrap(Config)` method** — runs after all keys exist, which is the right
  place to register cross-field repair. See
  [Cross-Value Validation](digging-deeper/cross-value-validation.md).

The two Stirling keys reference each other through `minValueOf` / `maxValueOf` suppliers so their
declaration order doesn't matter. The details are in
[Cross-Value Validation](digging-deeper/cross-value-validation.md).

## Next steps

- [Configuration Paths](the-basics/configuration-paths.md) — how `core.speed_multiplier` becomes
  a file and a JSON key.
- [The Bootstrap Convention](digging-deeper/bootstrap-convention.md) — the full lifecycle of
  `bootstrapConfig(...)`.

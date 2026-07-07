# Generated Files

Configory writes plain JSON. A config's **id** decides the file; its keys' **dotted paths** nest
inside that file. Seeing the exact output makes the [path rules](the-basics/configuration-paths.md)
concrete.

## The main config file

Keys defined on the main config (`config = configFor("examplemod")`) all live in one
`examplemod.json`:

```java
public static final ConfigKey<Float> SPEED_MULTIPLIER =
        config.defineFloat("core.speed_multiplier", 1.0f)
                .range(0.1f, 10.0f)
                .describe("Global speed multiplier.")
                .register();
```

writes:

```text
.minecraft/config/examplemod.json
```

```json
{
  "core": {
    "speed_multiplier": 1.0
  }
}
```

A dot-less path is just a top-level key (`defineFloat("speed_multiplier", 1.0f)` →
`{ "speed_multiplier": 1.0 }`).

## A second file

Declare an extra config with `configFor("examplemod", "engines")` and its keys land in
`config/examplemod/engines.json`:

```java
private static final Config engines = configFor(MOD_ID, "engines");

public static final ConfigKey<Double> STIRLING_MIN_OUTPUT =
        engines.defineDouble("stirling.min_output", 3.0)
                .min(0.0)
                .describe("Stirling engine minimum RF/t output.")
                .register();
```

writes:

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

The main `examplemod.json` and the `examplemod/` folder coexist side by side.
`bootstrapConfig(MOD_ID)` loads both.

> [!NOTE]
> Descriptions (`.describe(...)`) do **not** appear in the JSON — they're metadata for tooling,
> not file comments. See [Descriptions &amp; Introspection](digging-deeper/descriptions-and-introspection.md).

## Next steps

- [Configuration Paths](the-basics/configuration-paths.md) — the full id-to-file and nesting rules.
- [Custom Storage Backends](digging-deeper/custom-storage.md) — write somewhere other than disk.

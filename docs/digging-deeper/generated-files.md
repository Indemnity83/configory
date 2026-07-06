# Generated Files

Configory writes plain JSON, and the path you define maps directly onto the file and key layout.
Seeing the exact output makes the [path rules](the-basics/configuration-paths.md) concrete.

## A dotted path

```java
public static final ConfigKey<Float> SPEED_MULTIPLIER =
        config.defineFloat("core.speed_multiplier", 1.0f)
                .range(0.1f, 10.0f)
                .describe("Global speed multiplier.")
                .register();
```

writes:

```text
.minecraft/config/examplemod/core.json
```

```json
{
  "speed_multiplier": 1.0
}
```

## A nested path

```java
public static final ConfigKey<Double> STIRLING_MIN_OUTPUT =
        config.defineDouble("engines.stirling.min_output", 3.0)
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

## A bare key (single-file mode)

For a simple mod that only uses bare keys:

```java
public static final ConfigKey<Float> SPEED_MULTIPLIER =
        config.defineFloat("speed_multiplier", 1.0f)
                .range(0.1f, 10.0f)
                .register();
```

writes a single default file:

```text
.minecraft/config/examplemod/config.json
```

```json
{
  "speed_multiplier": 1.0
}
```

> [!NOTE]
> Descriptions (`.describe(...)`) do **not** appear in the JSON — they're metadata for tooling,
> not file comments. See [Descriptions &amp; Introspection](digging-deeper/descriptions-and-introspection.md).

## Next steps

- [Configuration Paths](the-basics/configuration-paths.md) — the full path-to-file rules.
- [Custom Storage Backends](digging-deeper/custom-storage.md) — write somewhere other than disk.

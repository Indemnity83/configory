# Cross-Value Validation

Some values depend on other config values — a minimum that must not exceed its paired maximum, for
example. Configory handles this with supplier-based bounds and sanitize hooks.

## Bounds tied to another key

`minValueOf` and `maxValueOf` bind a bound to another key instead of a fixed number:

```java
public static final ConfigKey<Double> STIRLING_MIN_OUTPUT =
        config.defineDouble("engines.stirling.min_output", 3.0)
                .min(0.0)
                .maxValueOf(() -> STIRLING_MAX_OUTPUT)
                .describe("Stirling engine minimum RF/t output.")
                .register();

public static final ConfigKey<Double> STIRLING_MAX_OUTPUT =
        config.defineDouble("engines.stirling.max_output", 10.0)
                .min(0.0)
                .minValueOf(() -> STIRLING_MIN_OUTPUT)
                .describe("Stirling engine maximum RF/t output.")
                .register();
```

Here the min may never exceed the max, and the max may never fall below the min — each expressed
as a constraint on the key it belongs to.

## Why suppliers

The bound is passed as a `Supplier<ConfigKey<T>>`, not the key itself, so that the two keys can
reference each other regardless of static initialization order. In the example above,
`STIRLING_MIN_OUTPUT` refers to `STIRLING_MAX_OUTPUT` before the latter is initialized — the
supplier defers that lookup until validation time, when both keys exist.

## Repairing invalid pairs

A per-key constraint can *reject* a bad value, but sometimes you want to *repair* a pair that has
drifted out of order — for instance when both values were edited on disk. Register a **sanitize
hook** for that. `repairMinMax` raises the minimum to equal the maximum when it has crept above
it:

```java
public static void bootstrap(Config config) {
    config.registerSanitizeHook(() ->
            config.repairMinMax(STIRLING_MIN_OUTPUT, STIRLING_MAX_OUTPUT));
}
```

Sanitize hooks run at the end of every load (after defaults and validation), which is why they're
the right place for cross-field repair that no single-key constraint can express. The
`bootstrap(Config)` method is the conventional place to register them — see
[The Bootstrap Convention](digging-deeper/bootstrap-convention.md).

## Next steps

- [The Bootstrap Convention](digging-deeper/bootstrap-convention.md) — where sanitize hooks are
  registered and when they run.
- [Descriptions &amp; Introspection](digging-deeper/descriptions-and-introspection.md) — surface
  your definitions to tooling.

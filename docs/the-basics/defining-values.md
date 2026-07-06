# Defining Values

A value is declared with a fluent chain: start with `config.define(path)`, choose a type, set a
default, add optional constraints, and finish with `register()` to get a typed `ConfigKey<T>`.

```java
config.define("core.speed_multiplier")   // start the chain at a path
        .asFloat()                       // choose the type
        .defaultValue(1.0f)              // required default
        .range(0.1f, 10.0f)              // optional constraints
        .describe("Global speed multiplier.")
        .register();                     // -> ConfigKey<Float>
```

> [!NOTE]
> A default value is required — `register()` throws if you omit it. `defaultValue(...)` also has
> the alias `defaultsTo(...)` if it reads better in your chain.

Configory supports the common primitive types. Each type has a matching `asX()` step and its own
constraints.

## Float

```java
public static final ConfigKey<Float> SPEED_MULTIPLIER =
        config.define("core.speed_multiplier")
                .asFloat()
                .defaultValue(1.0f)
                .range(0.1f, 10.0f)
                .describe("Global speed multiplier.")
                .register();
```

## Double

```java
public static final ConfigKey<Double> EFFICIENCY =
        config.define("machines.efficiency")
                .asDouble()
                .defaultValue(0.85)
                .range(0.0, 1.0)
                .describe("Machine efficiency multiplier.")
                .register();
```

## Integer

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

## Long

```java
public static final ConfigKey<Long> ENERGY_CAPACITY =
        config.define("machines.battery.capacity")
                .asLong()
                .defaultValue(100_000L)
                .min(0L)
                .describe("Battery energy capacity.")
                .register();
```

## Boolean

```java
public static final ConfigKey<Boolean> ENABLE_DEBUG =
        config.define("core.debug")
                .asBoolean()
                .defaultValue(false)
                .describe("Enables debug logging.")
                .register();
```

## String

Strings add `allowedValues(...)` to constrain the value to an enumeration:

```java
public static final ConfigKey<String> ENERGY_UNIT =
        config.define("core.energy_unit")
                .asString()
                .defaultValue("FE")
                .allowedValues("FE", "RF", "MJ")
                .describe("Displayed energy unit label.")
                .register();
```

## The steps at a glance

| Step | Available on | Purpose |
| --- | --- | --- |
| `asBoolean/asString/asInt/asLong/asFloat/asDouble()` | `define(path)` | choose the value type |
| `defaultValue(v)` / `defaultsTo(v)` | all types | required fallback value |
| `min(v)` / `max(v)` / `range(lo, hi)` | numeric types | fixed bounds |
| `minValueOf(...)` / `maxValueOf(...)` | numeric types | bounds tied to another key |
| `allowedValues(...)` | string | restrict to an enumeration |
| `describe(text)` | all types | human-readable description for tooling |
| `validator(...)` | all types | custom validation rule |
| `register()` | all types | build, register, and return the `ConfigKey<T>` |

Bounds and custom rules are covered in [Validation](digging-deeper/validation.md); the
`*ValueOf` variants in [Cross-Value Validation](digging-deeper/cross-value-validation.md);
descriptions in [Descriptions &amp; Introspection](digging-deeper/descriptions-and-introspection.md).

## Next steps

- [Reading Values](the-basics/reading-values.md) — use the keys you just defined.
- [Writing &amp; Saving](the-basics/writing-and-saving.md) — change and persist values.

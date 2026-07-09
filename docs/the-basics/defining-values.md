# Defining Values

A value is declared with a fluent chain: pick a type, set a default, add optional constraints, and
finish with `register()` to get a typed `ConfigKey<T>`. The everyday form is `defineX(path,
default)` — it names the type and presets the default in one call:

```java
config.defineFloat("core.speed_multiplier", 1.0f)   // type + default in one call
        .range(0.1f, 10.0f)                         // optional constraints
        .describe("Global speed multiplier.")
        .register();                                // -> ConfigKey<Float>
```

There is a shorthand per type — `defineBoolean`, `defineString`, `defineInt`, `defineLong`,
`defineFloat`, `defineDouble` — and each has a one-argument overload that leaves the default unset,
for when you'd rather set it lower in the chain:

```java
config.defineInt("machines.quarry.max_area")   // no preset default
        .defaultValue(64)
        .range(1, 256)
        .register();
```

Both forms are shorthand for the general chain, `define(path).asX()...`, which is still available
if you prefer to spell the type out as its own step:

```java
config.define("core.speed_multiplier")   // start at a path
        .asFloat()                       // choose the type
        .defaultValue(1.0f)              // set the default
        .register();
```

> [!NOTE]
> A default value is required — `register()` throws if you omit it. The two-argument
> `defineX(path, default)` supplies it up front; otherwise use `defaultValue(...)` (or its alias
> `defaultsTo(...)`) before `register()`.

Configory supports the common primitive types, shown below with the `defineX(...)` shorthand.

## Float

```java
public static final ConfigKey<Float> SPEED_MULTIPLIER =
        config.defineFloat("core.speed_multiplier", 1.0f)
                .range(0.1f, 10.0f)
                .describe("Global speed multiplier.")
                .register();
```

## Double

```java
public static final ConfigKey<Double> EFFICIENCY =
        config.defineDouble("machines.efficiency", 0.85)
                .range(0.0, 1.0)
                .describe("Machine efficiency multiplier.")
                .register();
```

## Integer

```java
public static final ConfigKey<Integer> MAX_AREA =
        config.defineInt("machines.quarry.max_area", 64)
                .min(1)
                .max(256)
                .describe("Maximum quarry area.")
                .register();
```

## Long

```java
public static final ConfigKey<Long> ENERGY_CAPACITY =
        config.defineLong("machines.battery.capacity", 100_000L)
                .min(0L)
                .describe("Battery energy capacity.")
                .register();
```

## Boolean

```java
public static final ConfigKey<Boolean> ENABLE_DEBUG =
        config.defineBoolean("core.debug", false)
                .describe("Enables debug logging.")
                .register();
```

## String

Strings add `allowedValues(...)` to constrain the value to an enumeration:

```java
public static final ConfigKey<String> ENERGY_UNIT =
        config.defineString("core.energy_unit", "FE")
                .allowedValues("FE", "RF", "MJ")
                .describe("Displayed energy unit label.")
                .register();
```

## The steps at a glance

| Step | Available on | Purpose |
| --- | --- | --- |
| `defineBoolean/defineString/defineInt/defineLong/defineFloat/defineDouble(path[, default])` | `config` | start a typed definition (shorthand for `define(path).asX()`) |
| `defineEnum(path, EnumType.class)` / `defineEnum(path, defaultConstant)` | `config` | start an enum definition (shorthand for `define(path).asEnum(...)`) |
| `asBoolean/asString/asInt/asLong/asFloat/asDouble()` | `define(path)` | choose the value type (long form) |
| `asEnum(EnumType.class)` | `define(path)` | type the value as an enum, stored by constant `name()` |
| `defaultValue(v)` / `defaultsTo(v)` | all types | required fallback value |
| `min(v)` / `max(v)` / `range(lo, hi)` | numeric types | fixed bounds |
| `minValueOf(...)` / `maxValueOf(...)` | numeric types | bounds tied to another key |
| `allowedValues(...)` | string | restrict to an enumeration |
| `describe(text)` | all types | human-readable description for tooling |
| `validator(...)` | all types | custom validation rule |
| `hidden()` | all types | exclude the key from the [generated command surface](digging-deeper/generated-commands.md) (keys are included by default) |
| `formerly(path)` | all types | migrate a value from a [former path](digging-deeper/renaming-keys.md) when the key is renamed (repeatable) |
| `register()` | all types | build, register, and return the `ConfigKey<T>` |

Bounds and custom rules are covered in [Validation](digging-deeper/validation.md); the
`*ValueOf` variants in [Cross-Value Validation](digging-deeper/cross-value-validation.md);
descriptions in [Descriptions &amp; Introspection](digging-deeper/descriptions-and-introspection.md).

## Next steps

- [Reading Values](the-basics/reading-values.md) — use the keys you just defined.
- [Writing &amp; Saving](the-basics/writing-and-saving.md) — change and persist values.

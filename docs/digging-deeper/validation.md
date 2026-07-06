# Validation

Validation is declared fluently during registration, right next to the value it guards. There is
no separate validation layer to wire up.

```java
public static final ConfigKey<Integer> MAX_AREA =
        config.define("machines.quarry.max_area")
                .asInt()
                .defaultValue(64)
                .range(1, 256)
                .describe("Maximum quarry area.")
                .register();
```

## Built-in constraints

| Constraint | Types | Meaning |
| --- | --- | --- |
| `min(v)` | numeric | value must be `>= v` |
| `max(v)` | numeric | value must be `<= v` |
| `range(lo, hi)` | numeric | shorthand for `min(lo).max(hi)` |
| `allowedValues(...)` | string | value must be one of the listed strings |
| `minValueOf(...)` / `maxValueOf(...)` | numeric | bound tied to another key — see [Cross-Value Validation](digging-deeper/cross-value-validation.md) |

## Custom validators

For rules the built-ins don't cover, add a `validator(...)`. It takes a `ConfigConstraint<T>` —
a function of `(value, config)` returning a `ValidationResult`:

```java
public static final ConfigKey<String> WORLD_NAME =
        config.define("core.world_name")
                .asString()
                .defaultValue("world")
                .validator((value, config) -> value.matches("[a-z0-9_]+")
                        ? ValidationResult.ok()
                        : ValidationResult.error("World name must be lowercase alphanumeric."))
                .register();
```

Return `ValidationResult.ok()` when the value passes, or `ValidationResult.error("…")` with a
message when it fails. Custom validators run in order alongside the built-in constraints, and the
`config` argument lets a rule reference other values if it needs to.

## When validation runs

Validation runs when:

- config files are loaded (and on every reload),
- defaults are applied,
- a typed value is set at runtime via `set(ConfigKey, value)`,
- sanitize hooks repair related values.

> [!ATTENTION]
> Setting a value by **string path** (`set(String, value)`) does not run validation — only the
> typed `set(ConfigKey, value)` does. See the note in
> [Writing &amp; Saving](the-basics/writing-and-saving.md).

## What happens to invalid values

Invalid values loaded from disk are not fatal. When a stored value fails validation (or is the
wrong type), Configory replaces it with the definition's default and marks the file dirty, so the
repaired value can be saved back cleanly. Your mod always sees a valid value; the file self-heals
on the next save.

Setting an invalid value through a typed key, by contrast, is rejected up front — `set` throws
`ConfigValidationException` rather than storing it. See [Error Handling](digging-deeper/error-handling.md).

## Next steps

- [Cross-Value Validation](digging-deeper/cross-value-validation.md) — rules that span two keys.
- [Error Handling](digging-deeper/error-handling.md) — the exception types involved.

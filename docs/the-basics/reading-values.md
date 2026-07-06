# Reading Values

There are two ways to read a value: through a typed `ConfigKey<T>` or through a string path. Use
typed keys in normal mod code and string paths for dynamic tools.

## Typed key access

Preferred for normal mod code — Java checks the type and there are no string typos:

```java
float speed = getConfig(Configs.SPEED_MULTIPLIER);
int maxArea = getConfig(Configs.MAX_AREA);
boolean debug = getConfig(Configs.ENABLE_DEBUG);
```

Reading a key returns its **default** when nothing is stored at the path. When a value *is*
present, it is coerced to the key's type and validated against the key's constraints before being
returned — see [Validation](digging-deeper/validation.md).

The equivalent from static code that doesn't have the host helpers:

```java
float speed = ConfigRegistry.config(MOD_ID).get(Configs.SPEED_MULTIPLIER);
```

## Path access

Useful for commands, terminals, debug tools, generated screens, or dynamic lookups. A path read
returns a `ConfigValue`, which you coerce with an `asX()` accessor:

```java
float speed = getConfig("core.speed_multiplier").asFloat();
int maxArea = getConfig("machines.quarry.max_area").asInt();
boolean debug = getConfig("core.debug").asBoolean();
```

Path access applies **no** definition constraints — it reads whatever raw value is stored. This
is deliberate: dynamic tools often need to see the actual on-disk value, not a validated one.

## Strict vs. fallback accessors

Each type has two accessors, and the difference matters:

- **Strict** — `asFloat()`, `asInt()`, `asString()`, … throw `ConfigException` if the value is
  missing, the wrong type, or (for numbers) out of range. The exception message includes the
  qualified path so you know exactly which value failed.
- **Fallback** — `asFloat(1.0f)`, `asInt(64)`, `asBoolean(false)`, … return the supplied default
  in exactly those cases instead of throwing.

```java
float strict = getConfig("core.speed_multiplier").asFloat();       // throws if absent/invalid
float safe = getConfig("core.speed_multiplier").asFloat(1.0f);     // 1.0f if absent/invalid
```

> [!TIP]
> When reading a path that may not exist, prefer the fallback overload. A `ConfigValue` currently
> has no presence check (such as `isPresent()`) — testing existence before a strict read means
> catching the exception or simply passing a fallback. Adding a presence check is tracked in the
> project's issue tracker.

## Next steps

- [Writing &amp; Saving](the-basics/writing-and-saving.md) — change values and persist them.
- [Error Handling](digging-deeper/error-handling.md) — the exception types and when they're thrown.

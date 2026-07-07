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

## Checking for a value

Call `isPresent()` to test whether a value is stored at a path before a strict read — useful for
dynamic tools that branch on presence rather than supply a default:

```java
ConfigValue value = getConfig("core.speed_multiplier");
if (value.isPresent()) {
    applySpeed(value.asFloat());
}
```

`isPresent()` reports whether a non-null value exists at the path; it does **not** check the
value's type, so a present value can still fail a strict `asX()` read if it is the wrong type.
`isEmpty()` is its inverse.

## Rendering a value for display

`asDisplayString()` renders whatever is stored — boolean, number, or string — as a string, so
command output and generated config screens don't have to switch on the value's type. It returns
`""` when the path is absent (pair with `isPresent()` to tell absent from empty):

```java
for (ConfigDefinition<?> def : config.definitions()) {
    String path = def.path().fullPath();
    player.sendMessage(path + " = " + getConfig(path).asDisplayString());
}
```

> [!TIP]
> When reading a path that may not exist, either test `isPresent()` first or use the fallback
> overload (`asFloat(1.0f)`) — both avoid a thrown `ConfigException`.

## Next steps

- [Writing &amp; Saving](the-basics/writing-and-saving.md) — change values and persist them.
- [Error Handling](digging-deeper/error-handling.md) — the exception types and when they're thrown.

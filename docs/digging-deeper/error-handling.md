# Error Handling

Configory reports problems with two unchecked exceptions. Both extend `RuntimeException`, so you
are never forced to catch them — but knowing which is thrown when helps you decide where to guard.

## The two exception types

- **`ConfigException`** — the general error: a missing `Configs` class, an unresolvable mod id, a
  duplicate definition, an unsupported value type, a strict path read that failed, a reload with
  unsaved changes, or a storage read/write failure.
- **`ConfigValidationException`** — a subclass of `ConfigException`, thrown specifically when a
  value fails validation against a key's constraints.

Because `ConfigValidationException extends ConfigException`, catching `ConfigException` catches
both; catch `ConfigValidationException` first when you want to treat validation failures
differently.

## What throws what

| Situation | Exception |
| --- | --- |
| `bootstrapConfig()` can't find a nested `Configs` class | `ConfigException` |
| No-arg `bootstrapConfig()` can't resolve `MOD_ID` / `MODID` | `ConfigException` |
| Two definitions registered at the same path | `ConfigException` |
| `register()` called with no default value | `ConfigException` |
| `set(String, value)` with an unsupported value type | `ConfigException` |
| Strict path read (`asInt()`, `asFloat()`, …) on a missing/mismatched/out-of-range value | `ConfigException` |
| `reload()` while there are unsaved changes | `ConfigException` |
| Storage can't read/parse or write a file | `ConfigException` |
| Using a `ConfigKey` with a config it doesn't belong to | `ConfigException` |
| `set(ConfigKey, value)` with a value that fails validation | `ConfigValidationException` |
| Reading a typed key whose stored value fails validation | `ConfigValidationException` |

## Values that heal instead of throw

Not every invalid value is an error. When a config **loads**, values on disk that are the wrong
type or fail validation are quietly replaced with their defaults and the file is marked dirty (so
the repaired value saves back). Loading never throws on a bad stored value — your mod always sees
a valid value. The throwing cases above are the ones you can act on programmatically; the healing
case is the one you don't need to. See [Validation](digging-deeper/validation.md).

## Example

```java
try {
    setConfig(Configs.MAX_AREA, 9999);   // typed set validates
} catch (ConfigValidationException e) {
    // out of the key's range — report it to the user, keep the old value
    player.sendMessage(e.getMessage());
}
```

## Next steps

- [Validation](digging-deeper/validation.md) — where validation runs and what heals.
- [Reading Values](the-basics/reading-values.md) — strict vs. fallback reads.

# Renaming Keys

When you rename a config key, players who already have the old name in their file would otherwise
lose their setting — it no longer matches the path you define, so it resets to the default. Declare
the former path with `.formerly(...)` and Configory carries the value forward on the next load.

```java
public static final ConfigKey<Float> SPEED_MULTIPLIER =
        config.defineFloat("core.speed_multiplier", 1.0f)
                .range(0.1f, 10.0f)
                .formerly("core.old_speed")   // the path this value used to live at
                .register();
```

On the next `load()`, a file that still has `core.old_speed` migrates to `core.speed_multiplier`,
keeping the player's value; a file already on the new name is untouched.

## How a rename resolves

The migration runs once per key during [`load()`](the-basics/reloading.md), alongside defaults and
validation:

- **Primary present** — the value at the current path is used as normal. Former paths are ignored.
- **Primary absent** — each former path is searched in the order declared; the first one that holds a
  value which **coerces to the key's type and passes its validation** is adopted at the primary path.
- **Nothing usable** — if no former path qualifies, the key falls back to its default, exactly as an
  unset key would.

Either way, every declared former path is then **stripped from the document**. The rename settles on
the next `save()`: the old key is gone from the file and the value lives under the new name. Because
the migration happens in the loaded document rather than in one accessor, every reader — typed keys,
[string paths](the-basics/reading-values.md), and the
[generated command surface](digging-deeper/generated-commands.md) — sees the migrated value.

A value that fails validation under a former path is skipped, not clamped, so a rename never
resurrects an out-of-range setting:

```java
config.defineFloat("core.speed_multiplier", 1.0f)
        .range(0.1f, 10.0f)
        .formerly("core.old_speed")   // tried first
        .formerly("legacy.speed")     // tried next if core.old_speed is absent or invalid
        .register();
```

## Migrating from a source Configory doesn't manage

Some values don't come from a former path in the same file — they come from another config, or need
to be computed. Pass a supplier instead of a path and it runs when the key is unset, producing the
value to migrate in:

```java
config.defineDouble("engines.max_output", 10.0)
        .formerly("engines.old_output")                 // try the old path first
        .formerly(() -> LegacyBridge.readMaxOutput())   // else compute or borrow it
        .register();                                     // else fall back to 10.0
```

Suppliers slot into the same ordered search as former paths: tried in declaration order, only when
the primary path is unset, and the first value that passes validation wins. The adopted value is
persisted on the next `save()`, so **the supplier runs once** — on the first load where the key is
unset — and never again. A supplier that returns `null` or throws is skipped, falling through to the
next source or the default, so a flaky source never breaks `load()`.

> [!WARNING]
> A supplier that reads **another Configory config** depends on that config already being loaded when
> this one loads. Configory does not order loads for you — this is an escape hatch you wire yourself.
> For a full move from a different config system, prefer
> [Seeding from a Legacy File](digging-deeper/seeding-from-legacy.md).

## Rules

- **Former paths are same-file.** A path resolves within the config's own document; a `Config` is one
  file. To move a value between files, give the new file its own config and
  [seed it](digging-deeper/seeding-from-legacy.md).
- **Clashes fail fast.** Registration throws `ConfigException` when a former path would overlap
  another path — whether by an exact match or by nesting inside or around it — be that a registered
  key, another key's former path, or its own key's path. This surfaces a rename mistake at startup
  instead of silently dropping data.
- **Retire former paths eventually.** A former path only matters until every file has migrated. Once
  you're confident old files are gone, drop the `.formerly(...)` call.

## When to seed instead

`.formerly(...)` is for a key **renamed within Configory** — same JSON file, different path. Moving
onto Configory from a different config system (a `.properties` file, another mod's format) is a
different job: read that source once and `trySet` the values across. See
[Seeding from a Legacy File](digging-deeper/seeding-from-legacy.md).

## Next steps

- [Defining Values](the-basics/defining-values.md) — where `.formerly(...)` fits in the builder chain.
- [Seeding from a Legacy File](digging-deeper/seeding-from-legacy.md) — migrating from a different
  config system entirely.

# Configuration Paths

Every value in Configory is addressed by a **path** — a string like `"core.speed_multiplier"`.
The path decides which file the value lives in and where inside that file's JSON it sits.

## One config instance per mod

Each consuming mod gets its own isolated config namespace. The mod id becomes the folder name
under the Minecraft config directory:

```text
.minecraft/config/logistics/core.json
.minecraft/config/logistics/engines.json
.minecraft/config/other_mod/core.json
```

Two mods can use identical paths without colliding, because each is scoped to its own folder.

## Dot notation

The **first** path segment names the config file. The **remaining** segments traverse the JSON
tree.

```java
"core.speed_multiplier"
```

maps to:

```text
config/<modid>/core.json
```

```json
{
  "speed_multiplier": 1.0
}
```

Nested paths keep traversing:

```java
"engines.stirling.min_output"
```

maps to:

```text
config/<modid>/engines.json
```

```json
{
  "stirling": {
    "min_output": 3.0
  }
}
```

## Single-file configs

Not every mod wants multiple files. A path with **no dot** is a bare key, and lands in the
default `config.json`:

```java
"speed_multiplier"
```

maps to:

```text
config/<modid>/config.json
```

```json
{
  "speed_multiplier": 1.0
}
```

`config` is the reserved name of that default file. Writing it out explicitly is allowed and is
just an alias — these two paths are identical:

```java
"speed_multiplier"          // bare key
"config.speed_multiplier"   // explicit alias — same file, same key
```

> [!NOTE]
> Paths always render in qualified form (`config.speed_multiplier`) in error messages and logs,
> so the backing file is never ambiguous. You can mix styles freely: bare keys share
> `config.json` while dotted keys split into their own files.

## Two ways to address a value

The same path can be reached two ways, and Configory supports both on purpose:

- **Typed keys** (`ConfigKey<T>`) for normal mod code — Java checks the type and there are no
  string typos. Covered in [Defining Values](the-basics/defining-values.md).
- **String paths** for dynamic tools — commands, terminals, debug menus, generated screens, and
  scripts. Covered in [Reading Values](the-basics/reading-values.md).

## Next steps

- [Defining Values](the-basics/defining-values.md) — declare a typed key for a path.
- [Reading Values](the-basics/reading-values.md) — read a path by key or by string.

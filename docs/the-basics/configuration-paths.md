# Configuration Paths

Every value in Configory is addressed by a **path** — a string like `"core.speed_multiplier"`.
Within a config, every dot in a path is a **nesting boundary** into the JSON tree. *Which file* the
value lives in is decided by the config's **id**, not by the path.

## The config id decides the file

Each `Config` is one JSON file, and its id is where that file lives under the Minecraft config
directory. A plain id is a flat file; a dotted id nests into subdirectories:

```text
configFor("logistics")            ->  config/logistics.json
configFor("logistics", "engines") ->  config/logistics/engines.json
```

So a simple mod is a single `config/<modid>.json`; a mod that wants multiple files declares extra
configs (see [Recommended Structure](getting-started/recommended-structure.md)), which land in a
`config/<modid>/` folder. Two mods never collide because each is scoped to its own id.

## Dot notation is pure nesting

Every dot nests one level deeper into the config's document.

```java
"core.speed_multiplier"
```

in `config/<modid>.json` is:

```json
{
  "core": {
    "speed_multiplier": 1.0
  }
}
```

Nested paths keep traversing:

```java
"engines.stirling.min_output"
```

is:

```json
{
  "engines": {
    "stirling": {
      "min_output": 3.0
    }
  }
}
```

A path with **no dot** is simply a top-level key:

```java
"speed_multiplier"
```

is `{ "speed_multiplier": 1.0 }`.

> [!NOTE]
> Because dots only ever nest, `speed` and `speed.max` live in the **same** file, nested together —
> there is no hidden "first segment is the file" rule. To put values in a separate file, give them
> their own config id.

## Two ways to address a value

The same path can be reached two ways, and Configory supports both on purpose:

- **Typed keys** (`ConfigKey<T>`) for normal mod code — Java checks the type and there are no
  string typos. Covered in [Defining Values](the-basics/defining-values.md).
- **String paths** for dynamic tools — commands, terminals, debug menus, generated screens, and
  scripts. Covered in [Reading Values](the-basics/reading-values.md).

## Next steps

- [Defining Values](the-basics/defining-values.md) — declare a typed key for a path.
- [Reading Values](the-basics/reading-values.md) — read a path by key or by string.

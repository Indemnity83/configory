# Introduction

Minecraft mod configuration often ends up being a mix of boilerplate, static constants,
validation code, config file handling, command integration, and awkward call sites. Configory
aims to make the common case pleasant while keeping type safety where mod code needs it.

## What Configory gives you

- **Typed config keys** — `ConfigKey<T>` constants that Java can type-check, so mod code never
  guesses at value types or misspells a path.
- **Dot-notation paths** — a single string like `"core.speed_multiplier"` addresses a value,
  which is ideal for commands, debug tools, and generated screens.
- **A single-file mode** — simple mods can use bare keys that all land in one `config.json`.
- **Fluent validation** — declare ranges, allowed values, and custom rules right next to the
  definition.
- **Per-mod isolation** — each mod gets its own folder under the Minecraft config directory.
- **Explicit save and reload** — runtime mutation never surprises you by writing to disk; you
  decide when to persist.
- **A lightweight bootstrap convention** — one call wires up definition, loading, defaults,
  validation, and repair.

## The shape of it

Define a value once, with its type, default, and constraints:

```java
public static final ConfigKey<Float> SPEED_MULTIPLIER =
        config.define("core.speed_multiplier")
                .asFloat()
                .defaultValue(1.0f)
                .range(0.1f, 10.0f)
                .describe("Global speed multiplier.")
                .register();
```

Then use it safely from mod code:

```java
float speed = getConfig(Configs.SPEED_MULTIPLIER);
```

The string path is still there whenever you want terminal commands, debugging, scripts, or
dynamic tools:

```java
float speed = getConfig("core.speed_multiplier").asFloat();
```

## What Configory is not

Configory is intentionally small. It is not a settings UI, a command framework, or a complete
config ecosystem — it is the config foundation those things can build on. See
[Design Goals &amp; Roadmap](reference/design-goals.md) for the boundaries and where it may grow.

## Next steps

- [Installation](getting-started/installation.md) — add Configory to your build.
- [Quick Start](getting-started/quick-start.md) — the smallest working setup.
- [Recommended Mod Structure](getting-started/recommended-structure.md) — where to put your keys.

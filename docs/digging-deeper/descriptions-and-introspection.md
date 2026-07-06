# Descriptions &amp; Introspection

Configory keeps enough metadata about each value that tools can render, document, and generate
config surfaces without hard-coding anything.

## Descriptions

Every config key can carry a description:

```java
config.define("engines.redstone.output")
        .asLong()
        .defaultValue(10L)
        .describe("RF generated per 16-tick interval.")
        .register();
```

Descriptions are intended for:

- terminal commands,
- generated config documentation,
- future config screens,
- schema generation,
- tooltips,
- help output.

> [!NOTE]
> Configory uses `.describe(...)` rather than `.comment(...)` on purpose: descriptions are not
> limited to file comments. They're metadata that any tool can read, not text baked into a
> `.json` file.

## Introspecting definitions

A config exposes every registered definition, which is what makes generated tooling possible:

```java
Config config = ConfigRegistry.config(MOD_ID);

for (ConfigDefinition<?> def : config.definitions()) {
    System.out.println(def.path().fullPath() + " (" + def.type() + ")");
    System.out.println("  default: " + def.defaultValue());
    System.out.println("  " + def.description());
}
```

`config.definitions()` returns an unmodifiable view of all definitions in registration order.

### What a `ConfigDefinition` exposes

| Accessor | Returns |
| --- | --- |
| `path()` | the value's `ConfigPath` (use `fullPath()` for the qualified string) |
| `type()` | the declared `ConfigType` (`BOOLEAN`, `STRING`, `INT`, `LONG`, `FLOAT`, `DOUBLE`) |
| `valueClass()` | the Java class of the value |
| `defaultValue()` | the default used when unset or invalid |
| `description()` | the human-readable description, or an empty string |
| `constraints()` | the immutable list of validation constraints, in order |

This is the raw material for a `/config` command that lists keys, a generated Markdown reference,
or a config screen that renders each value with its default and description.

## Next steps

- [Custom Storage Backends](digging-deeper/custom-storage.md) — swap where values are persisted.
- [API Summary](reference/api-summary.md) — the full method surface.

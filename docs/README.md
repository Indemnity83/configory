<!-- docs/README.md -->

# Configory

**Convention-based configuration for Minecraft mods.**

Configory is a small Java configuration library for Minecraft mods that combines dot-notation
paths (or a single default file for simple mods), typed config keys, fluent validation, per-mod
config isolation, JSON-backed files, runtime mutation with explicit saving, and lightweight
bootstrap conventions.

It is designed to make configuration feel simple at the call site without giving up type safety
where mod code needs it.

```java
float speed = getConfig(Configs.SPEED_MULTIPLIER);

setConfig(Configs.SPEED_MULTIPLIER, 3.0f).save();
```

Or, when you need dynamic path access:

```java
float speed = getConfig("core.speed_multiplier").asFloat();

setConfig("core.speed_multiplier", 3.0f).save();
```

## Where to start

- **New here?** Read the [Introduction](getting-started/introduction.md), then the
  [Quick Start](getting-started/quick-start.md).
- **Building a typical mod?** [The Basics](the-basics/configuration-paths.md) covers the 80%
  happy path — defining, reading, writing, and reloading values.
- **Doing something unconventional?** [Digging Deeper](digging-deeper/validation.md) covers
  validation, custom storage, manual access, error handling, and more.
- **Looking for a specific method?** Jump to the [API Summary](reference/api-summary.md).

## Documentation map

| Section | What's inside |
| --- | --- |
| [Getting Started](getting-started/introduction.md) | Introduction, installation, quick start, recommended structure |
| [The Basics](the-basics/configuration-paths.md) | Paths, defining values, reading, writing &amp; saving, reloading |
| [Digging Deeper](digging-deeper/validation.md) | Validation, cross-value rules, introspection, bootstrap, manual access, custom storage, error handling, generated files |
| [Reference](reference/api-summary.md) | API summary, design goals &amp; roadmap |

## License

Configory is licensed under the MIT License.

Minecraft is a trademark of Microsoft. Configory is not affiliated with or endorsed by Mojang
Studios or Microsoft.

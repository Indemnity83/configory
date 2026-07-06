# Writing &amp; Saving

Writing is a two-step story on purpose: `set` changes the value in memory, and `save` writes it
to disk. Nothing is persisted until you ask for it.

## Runtime-only mutation

`set` updates the runtime value and marks the backing file dirty. It does **not** write to disk:

```java
setConfig(Configs.SPEED_MULTIPLIER, 3.0f);
```

At this point the new value is live for any subsequent read, and the file it belongs to is
flagged as having unsaved changes.

## Set and save

Chain `.save()` on the result to persist just the file affected by that mutation:

```java
setConfig(Configs.SPEED_MULTIPLIER, 3.0f).save();
```

The chained save touches only the one file, leaving other dirty files untouched.

## Save all dirty files

To flush every file with unsaved changes at once:

```java
saveConfig();
```

Or, going through the config instance directly:

```java
ConfigRegistry.config(MOD_ID).save();
```

## Save one file

To save a single named file explicitly:

```java
ConfigRegistry.config(MOD_ID).save("core");
```

## Inspecting dirty state

You can ask a config whether it has unsaved changes, and which files are affected:

```java
Config config = ConfigRegistry.config(MOD_ID);

boolean pending = config.isDirty();
Set<String> files = config.dirtyFiles();   // e.g. ["core", "engines"]
```

`dirtyFiles()` returns an immutable snapshot of the file names (first path segments, without the
`.json` extension) that would be written by the next `save()`.

## Validation on write

> [!ATTENTION]
> Typed and string-path writes validate differently. `set(ConfigKey, value)` validates against
> the key's constraints and throws `ConfigValidationException` if the value is invalid.
> `set(String, value)` does **not** validate — it writes the raw value straight through, even
> when a definition with constraints exists at that path. This is deliberate: a raw string write
> lets tools persist the exact on-disk value.

To validate a write instead of writing raw, use `trySet(...)`. It checks the value against a
definition's constraints and reports success rather than throwing — ideal for commands and other
dynamic tools handling untrusted input:

```java
if (config.trySet("core.speed_multiplier", userInput)) {
    config.save();   // accepted and persisted
} else {
    // rejected: out of range, wrong type, or otherwise invalid
}
```

`trySet` accepts a string path or a typed key. When a definition exists at the path it coerces and
validates the value, writing only if it passes; where no definition exists there are no constraints
to apply, so the value is written and `true` is returned. See
[Validation](digging-deeper/validation.md).

## Next steps

- [Reloading](the-basics/reloading.md) — re-read values from disk, safely.
- [Validation](digging-deeper/validation.md) — declare and understand constraints.

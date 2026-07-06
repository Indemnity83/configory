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
> when a definition with constraints exists at that path. Reconciling this asymmetry (for example
> a validating `trySet(...)`) is tracked in the project's issue tracker.

For dynamic tools that must validate user input today, read through the typed key's `set` path,
or validate the input yourself before a string-path write. See
[Validation](digging-deeper/validation.md).

## Next steps

- [Reloading](the-basics/reloading.md) — re-read values from disk, safely.
- [Validation](digging-deeper/validation.md) — declare and understand constraints.

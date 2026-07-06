# Reloading

Reloading reads values from disk again, replacing the in-memory state. It's how you pick up
changes a user made to the config files while the game is running.

```java
reloadConfig();
```

## Safe by default

`reloadConfig()` refuses to silently throw away unsaved runtime changes. If any file is dirty,
it throws rather than clobbering your in-memory mutations:

```java
setConfig(Configs.SPEED_MULTIPLIER, 3.0f);   // dirty, not saved
reloadConfig();                              // throws — unsaved changes exist
```

This protects against accidental data loss: reload the fresh values, or save your changes first —
but never lose work without asking.

## Discarding changes on purpose

When you *do* want to throw away unsaved runtime changes and reload from disk, be explicit:

```java
ConfigRegistry.config(MOD_ID).discardAndReload();
```

`discardAndReload()` never throws on dirty state; any in-memory mutations that were not saved are
lost.

## What a reload does

A reload re-reads every file referenced by a registered key and then runs the same post-load
processing as the initial load: missing keys get their defaults, invalid or wrong-typed values on
disk are replaced with defaults (and their files marked dirty so the repaired values can be saved
back), and any sanitize hooks run. See [The Bootstrap Convention](digging-deeper/bootstrap-convention.md)
for the full lifecycle and [Validation](digging-deeper/validation.md) for the repair behavior.

## Next steps

- [Validation](digging-deeper/validation.md) — what happens to invalid values on load.
- [Error Handling](digging-deeper/error-handling.md) — the exception thrown by a refused reload.

# Seeding from a Legacy Config File

When a mod moves onto Configory from an older config system, you usually want to carry the user's
existing settings over on first run instead of resetting everything to defaults.

> [!NOTE]
> This is for migrating from a **different** config system. If you've merely renamed a key **within**
> Configory — same file, new path — reach for [`.alias(...)`](digging-deeper/renaming-keys.md)
> instead; it carries the value forward with no bootstrap code.

The pattern is: after bootstrap, read the legacy file **once**, parse each value and push it in with
[`trySet`](the-basics/writing-and-saving.md) — both steps guard against bad data, so nothing throws —
then `save()` and retire the legacy file so the migration never runs again.

## Recipe

```java
public final class ExampleMod implements ConfigHost {
    public static final String MOD_ID = "examplemod";

    public void initCommon() {
        bootstrapConfig(MOD_ID);
        // legacyFile is wherever your old system wrote — e.g. <configDir>/examplemod.properties
        seedFromLegacy(config(), Path.of("config", "examplemod.properties"));
    }

    private static void seedFromLegacy(Config config, Path legacyFile) {
        if (!Files.exists(legacyFile)) {
            return; // fresh install, or already migrated
        }

        Properties legacy = new Properties();
        try (var in = Files.newInputStream(legacyFile)) {
            legacy.load(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        // trySet takes an already-typed value, so parse the legacy strings defensively first: a
        // malformed entry is skipped here instead of throwing. trySet then skips any parsed value
        // that fails the definition's type/range checks.
        parseFloat(legacy.getProperty("speed"))
                .ifPresent(v -> config.trySet("core.speed_multiplier", v));
        parseDouble(legacy.getProperty("stirling_min"))
                .ifPresent(v -> config.trySet("engines.stirling.min_output", v));

        config.save();

        try {
            Files.move(legacyFile, legacyFile.resolveSibling(legacyFile.getFileName() + ".migrated"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Optional<Float> parseFloat(String raw) {
        try {
            return raw == null ? Optional.empty() : Optional.of(Float.parseFloat(raw.trim()));
        } catch (NumberFormatException e) {
            return Optional.empty(); // malformed legacy value — skip it
        }
    }

    private static Optional<Double> parseDouble(String raw) {
        try {
            return raw == null ? Optional.empty() : Optional.of(Double.parseDouble(raw.trim()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
```

## Why `trySet`

`trySet(path, value)` takes an **already-typed** value (a `Float`, `Double`, `Boolean`, ...), checks
it against whatever definition is registered at that path, and writes only when it fits — returning
`false` otherwise (see [Writing &amp; Saving](the-basics/writing-and-saving.md)). It does **not** parse
strings: pass `"3.5"` to a float key and it is rejected, because a string isn't a float.

That splits legacy migration into two guarded steps, neither of which throws:

1. **Parse** each legacy string to its target type yourself, catching `NumberFormatException` so a
   malformed entry (`speed=fast`) is skipped — the `parseFloat`/`parseDouble` helpers above do this.
2. **`trySet`** the parsed value, which skips anything out of range or of the wrong type, leaving the
   Configory default in place.

## Notes

- **Run it after `bootstrapConfig`**, so defaults are already applied; legacy values then override
  only the keys they actually cover.
- **Retire the legacy file** (rename or delete) once migrated so the seed runs exactly once. Guarding
  on its existence keeps every later launch a no-op.
- **A structured source works the same way.** Reading a different JSON schema, a database, or another
  mod's file is the same shape: read it, `trySet` the mapped paths, `save()`.
- **Prefer the explicit mapping** above to a clever one. A one-shot custom
  [ConfigStorage](digging-deeper/custom-storage.md) that reads the old format on first `load()` is
  possible, but the `trySet` mapping is clearer and easier to evolve as keys change.

## Next steps

- [Writing &amp; Saving](the-basics/writing-and-saving.md) — `set`, `trySet`, and persistence.
- [Custom Storage Backends](digging-deeper/custom-storage.md) — the storage-swap alternative.

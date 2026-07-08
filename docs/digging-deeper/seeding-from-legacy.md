# Seeding from a Legacy Config File

When a mod moves onto Configory from an older config system, you usually want to carry the user's
existing settings over on first run instead of resetting everything to defaults.

The pattern is: after bootstrap, read the legacy file **once**, push its values in with
[`trySet`](the-basics/writing-and-saving.md), `save()`, and then retire the legacy file so the
migration never runs again.

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

        // Map each legacy key onto a Configory path. trySet validates and skips values that don't fit.
        if (legacy.containsKey("speed")) {
            config.trySet("core.speed_multiplier", Float.parseFloat(legacy.getProperty("speed")));
        }
        if (legacy.containsKey("stirling_min")) {
            config.trySet("engines.stirling.min_output", Double.parseDouble(legacy.getProperty("stirling_min")));
        }

        config.save();

        try {
            Files.move(legacyFile, legacyFile.resolveSibling(legacyFile.getFileName() + ".migrated"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
```

## Why `trySet`

`trySet(path, value)` coerces and validates against whatever definition is registered at that path,
writing only when the value fits and returning `false` otherwise (see
[Writing &amp; Saving](the-basics/writing-and-saving.md)). That makes it the safe choice for untrusted
legacy data: an out-of-range or wrong-typed legacy value is skipped — leaving the Configory default
in place — rather than throwing and aborting the whole migration.

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

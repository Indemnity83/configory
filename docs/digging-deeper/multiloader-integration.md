# Multiloader Integration

A guide to dropping Configory into a real Fabric + NeoForge mod: where config code lives, how to
bundle Configory so users don't install it separately, the runtime config-directory caveat, and how
to unit-test config code. This page assumes the [Installation](getting-started/installation.md)
dependency is already set up.

## Where config code lives

Configory's core is pure Java — no Minecraft or loader imports — so **all of your config definitions
belong in your common source set**, shared by both loaders. Only the entrypoint that calls
`bootstrapConfig(...)` is loader-specific.

```text
common/    ExampleMod (ConfigHost) + nested Configs holder + every ConfigKey
fabric/    ModInitializer      -> new ExampleMod().initCommon()
neoforge/  @Mod class          -> new ExampleMod().initCommon()
```

```java
// common — the host, the holder, and all keys
public final class ExampleMod implements ConfigHost {
    public static final String MOD_ID = "examplemod";

    public void initCommon() {
        bootstrapConfig(MOD_ID);
    }

    public static final class Configs extends ConfigEntries {
        static final Config config = configFor(MOD_ID);

        public static final ConfigKey<Float> SPEED =
                config.defineFloat("core.speed_multiplier", 1.0f).range(0.1f, 10f).register();
    }
}
```

```java
// fabric
public final class ExampleModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        new ExampleMod().initCommon();
    }
}
```

```java
// neoforge
@Mod(ExampleMod.MOD_ID)
public final class ExampleModNeoForge {
    public ExampleModNeoForge() {
        new ExampleMod().initCommon();
    }
}
```

Because the `Configs` holder and its `ConfigKey` constants live in common, both loaders read and
write the same files through the same typed keys.

## Bundling Configory

Configory is a **library mod** — it ships `fabric.mod.json`, `neoforge.mods.toml`, and
`FMLModType: GAMELIBRARY` — so you can nest it inside your own jar instead of asking users to
download it separately.

> [!NOTE]
> The exact bundling DSL depends on your Loom / ModDevGradle version; the forms below are the common
> ones. Confirm the nested jar actually loads on both loaders — end-to-end bundling verification is
> tracked in [#28](https://github.com/Indemnity83/configory/issues/28).

**Fabric (Loom)** — `include` nests it as a Jar-in-Jar dependency:

```gradle
dependencies {
    modImplementation "maven.modrinth:configory:%%VERSION%%"
    include           "maven.modrinth:configory:%%VERSION%%"
}
```

**NeoForge (ModDevGradle)** — `jarJar` nests it:

```gradle
dependencies {
    implementation "maven.modrinth:configory:%%VERSION%%"
    jarJar("maven.modrinth:configory:%%VERSION%%")
}
```

Both resolve from the Modrinth Maven repository added in
[Installation](getting-started/installation.md).

## The config-directory caveat

`Config.create(id)` roots its files at `Path.of("config")` — **relative to the process working
directory**, which at runtime is the game directory, so files land in `.minecraft/config/<id>.json`.
That is correct for a normal launch on either loader, with no extra wiring.

Point at an explicit directory when you'd rather not rely on the working directory — for example a
loader-supplied config dir:

```java
Path dir = FabricLoader.getInstance().getConfigDir();   // NeoForge: FMLPaths.CONFIGDIR.get()
bootstrapConfig(MOD_ID, new JsonFileConfigStorage(dir));
```

> [!NOTE]
> Injected storage roots the **main** config only; child configs from `configFor(MOD_ID, "<name>")`
> still use the default `config/` location. To relocate a child too, create it directly with
> `Config.create(id, storage)`. See [Custom Storage Backends](digging-deeper/custom-storage.md).

## Unit-testing config code

Config code is plain Java, so it tests without a running game. Point a `JsonFileConfigStorage` at a
JUnit `@TempDir`:

```java
@Test
void speedDefaultsAndValidates(@TempDir Path dir) {
    Config config = Config.create("examplemod", new JsonFileConfigStorage(dir));
    ConfigKey<Float> speed =
            config.defineFloat("core.speed_multiplier", 1.0f).range(0.1f, 10f).register();
    config.load();

    assertEquals(1.0f, config.get(speed));                                       // default applied
    assertThrows(ConfigValidationException.class, () -> config.set(speed, 99f)); // out of range

    config.set(speed, 2.5f).save();
    assertTrue(Files.exists(dir.resolve("examplemod.json")));                    // persisted
}
```

For faster, disk-free tests, back the config with a tiny in-memory `ConfigStorage`:

```java
final class MapStorage implements ConfigStorage {
    private final Map<String, JsonObject> files = new HashMap<>();

    @Override
    public JsonObject load(String id) {
        JsonObject doc = files.get(id);
        return doc == null ? new JsonObject() : doc.deepCopy();
    }

    @Override
    public void save(String id, JsonObject root) {
        files.put(id, root.deepCopy());
    }
}
```

## Next steps

- [The Bootstrap Convention](digging-deeper/bootstrap-convention.md) — the full `bootstrapConfig` lifecycle.
- [Custom Storage Backends](digging-deeper/custom-storage.md) — swap where and how configs persist.
- [Seeding from a Legacy File](digging-deeper/seeding-from-legacy.md) — migrate a user's existing settings.

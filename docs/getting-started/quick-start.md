# Quick Start

This is the smallest setup that defines, loads, validates, and uses a config value.

## 1. Implement `ConfigHost`

Your mod entry point implements `ConfigHost`, which mixes in ready-made `getConfig` /
`setConfig` / `saveConfig` / `reloadConfig` helpers bound to your mod's own config.

```java
public final class ExampleMod implements ConfigHost {
    public static final String MOD_ID = "examplemod";

    public void initCommon() {
        bootstrapConfig(MOD_ID);
    }

    public static final class Configs extends ConfigEntries {
        private static final Config config = configFor(MOD_ID);

        private Configs() {}

        public static final ConfigKey<Float> SPEED_MULTIPLIER =
                config.define("core.speed_multiplier")
                        .asFloat()
                        .defaultValue(1.0f)
                        .range(0.1f, 10.0f)
                        .describe("Global speed multiplier.")
                        .register();
    }
}
```

That is enough. The nested `Configs` class holds your keys, and `bootstrapConfig(MOD_ID)` wires
everything up during mod init. See the [Bootstrap Convention](digging-deeper/bootstrap-convention.md)
for exactly what that call does.

> [!TIP]
> `initCommon()` here is your mod's own init method — call it from whatever your mod loader
> invokes at startup. Only `bootstrapConfig(...)` is part of Configory.

## 2. Read config values

From your mod entry point (or anywhere with access to the host helpers):

```java
float speed = getConfig(Configs.SPEED_MULTIPLIER);
```

From static code that doesn't have the host helpers, go through the registry:

```java
float speed = ConfigRegistry.config(ExampleMod.MOD_ID)
        .get(ExampleMod.Configs.SPEED_MULTIPLIER);
```

Or read by path directly:

```java
float speed = ConfigRegistry.config(ExampleMod.MOD_ID)
        .get("core.speed_multiplier")
        .asFloat();
```

## 3. Set config values

`set` changes the runtime value only — it does not touch disk:

```java
setConfig(Configs.SPEED_MULTIPLIER, 3.0f);
```

Chain `.save()` to persist just the affected file:

```java
setConfig(Configs.SPEED_MULTIPLIER, 3.0f).save();
```

Or save every dirty file at once:

```java
saveConfig();
```

## Next steps

- Learn how paths map to files in [Configuration Paths](the-basics/configuration-paths.md).
- Organize a real mod with the [Recommended Mod Structure](getting-started/recommended-structure.md).
- Explore [Defining Values](the-basics/defining-values.md) for every supported type.

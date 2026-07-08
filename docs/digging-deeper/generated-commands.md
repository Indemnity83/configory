# Generated Commands

Configory can build a `/gamerule`-style in-game command surface for your config, so you don't
hand-write the command bridge. Every key is included by default; opt any one out with `.hidden()`.

## Hide the keys you don't want in commands

Opt a key **out** with `.hidden()` — a setting driven by its own command, or one you'd rather
players not change:

```java
public static final class Configs extends ConfigEntries {
    static final Config config = configFor(MOD_ID);

    // in the commands by default
    public static final ConfigKey<Double> MAX_OUTPUT =
            config.defineDouble("engines.max_output", 10.0).min(0.0).register();

    public static final ConfigKey<Difficulty> DIFFICULTY =
            config.defineEnum("core.difficulty", Difficulty.NORMAL).register();

    // .hidden() keeps it out of the command
    static final ConfigKey<String> SECRET =
            config.defineString("core.secret", "...").hidden().register();
}
```

## Register the command

`ConfigCommands` — the only Brigadier-aware part of Configory — turns the exposed keys into a
`/gamerule`-style command. Register it from your loader's command event, passing your command
`dispatcher` and a one-line feedback callback that delivers a `String` to a command source:

```java
ConfigCommands.register(dispatcher, MOD_ID, config, (source, message) -> /* send message to source */);
```

That produces:

```text
/<modid> config                 list every key and its current value
/<modid> config <key>           show one value
/<modid> config <key> <value>   set it
/<modid> reload-configs         reload the config from disk
```

- **`config <key> <value>`** parses `<value>` with Brigadier — typed per key (a double arg for a
  double key, tab suggestions for an enum) — then runs it through the key's validation; an
  out-of-range or unknown value is rejected with feedback and nothing changes.
- **`config <key>`** and bare **`config`** render current values via
  [`asDisplayString()`](the-basics/reading-values.md).
- **`reload-configs`** re-reads the config from disk.

Brigadier merges command trees that share a root literal, so `/<modid> config ...` coexists with any
other `/<modid> ...` commands your mod registers (as long as none also claims a `config` child).

## Multiple config files

A mod with more than one config (see
[`configFor(MOD_ID, "engines")`](getting-started/recommended-structure.md)) uses the builder: the
**main** config's keys stay native under `config`, and each extra file nests one layer deeper as a
**group**.

```java
ConfigCommands.forRoot(MOD_ID, feedback)
        .add(config)                    // keys native under /<modid> config
        .group("engines", engineConfig)  // /<modid> config engines <key> ...
        .group(pumpConfig)               // group name from the config id's last segment
        .register(dispatcher);
```

produces:

```text
/<modid> config <key> [value]            main config keys
/<modid> config engines <key> [value]    the "engines" group
/<modid> config pumps <key> [value]      the "pumps" group
/<modid> reload-configs                  reload the main config and every group
```

Native keys and group names share one level, so a **native key whose literal equals a group name**
would collide — keep them distinct (a mod usually uses one shape or the other).

## Restricting who can change config

By default anyone who can run the command can set values and reload. To gate them — usually to
operators — add `.requires(...)` to the builder; it applies to the `config` and `reload-configs`
nodes (not the root), so the tree still merges with your other `/<modid> ...` commands:

```java
ConfigCommands.forRoot(MOD_ID, feedback)
        .add(config)
        .requires(source -> source.hasPermissionLevel(2))  // Fabric; NeoForge: source.hasPermission(2)
        .register(dispatcher);
```

## Loader wiring

The registration call lives in each loader's command event — the only loader-specific glue. The
exact feedback signature varies by version; adapt the callback to yours.

> [!WARNING]
> `set` and `reload-configs` mutate your config, and the plain `register(...)` shortcut adds **no
> permission check**. On a shared or public server, gate them with `.requires(...)` as shown above.

**Fabric**
```java
CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) ->
        ConfigCommands.register(dispatcher, MOD_ID, config,
                (src, msg) -> src.sendFeedback(() -> Text.literal(msg), false)));
```

**NeoForge**
```java
@SubscribeEvent
void onRegisterCommands(RegisterCommandsEvent event) {
    ConfigCommands.register(event.getDispatcher(), MOD_ID, config,
            (src, msg) -> src.sendSuccess(() -> Component.literal(msg), false));
}
```

Because the feedback callback is the only thing that touches a Minecraft API, the command builder
itself is loader- and version-agnostic. Brigadier is provided by the game at runtime — Configory
compiles against it but never bundles it, and consumers don't inherit it transitively.

## Next steps

- [Defining Values](the-basics/defining-values.md) — where `.hidden()` fits in the builder chain.
- [Multiloader Integration](digging-deeper/multiloader-integration.md) — common-vs-loader placement.

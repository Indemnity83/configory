# Generated Commands

Configory can build an in-game command surface for your config — `list`, `get`, `set`, and
`reload` — so you don't hand-write the command bridge. Every key is included by default; opt any
one out with `.hidden()`.

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
`config` command node. Register it from your loader's command event, passing your command
`dispatcher` and a one-line feedback callback that delivers a `String` to a command source:

```java
ConfigCommands.register(dispatcher, MOD_ID, config, (source, message) -> /* send message to source */);
```

That produces:

```text
/<modid> config list
/<modid> config get <key>
/<modid> config set <key> <value>
/<modid> config reload
```

- **`set`** parses `<value>` with Brigadier — typed per key (a double arg for a double key, tab
  suggestions for an enum) — then runs it through the key's validation; an out-of-range or unknown
  value is rejected with feedback and nothing changes.
- **`get`** / **`list`** render current values via
  [`asDisplayString()`](the-basics/reading-values.md).
- **`reload`** re-reads the file from disk.

Brigadier merges command trees that share a root literal, so `/<modid> config ...` coexists with any
other `/<modid> ...` commands your mod registers (as long as none also claims a `config` child).

### Placing the node yourself

For a permission wrapper or a different location, take the node instead of using the convenience:

```java
dispatcher.register(literal(MOD_ID)
        .requires(source -> hasPermission(source))
        .then(ConfigCommands.configNode(config, feedback)));
```

## Loader wiring

The registration call lives in each loader's command event — the only loader-specific glue. The
exact feedback signature varies by version; adapt the callback to yours:

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

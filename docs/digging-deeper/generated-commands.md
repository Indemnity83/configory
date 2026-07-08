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

### Restricting who can change config

`register(...)` applies **no permission check**, so anyone who can run the command can `set` and
`reload`. To gate mutation — usually to operators — attach a `.requires(...)` to the node and
register it yourself instead of using the convenience (this also lets you place the node elsewhere):

```java
dispatcher.register(literal(MOD_ID).then(
        ConfigCommands.configNode(config, feedback)
                .requires(source -> source.hasPermissionLevel(2))));  // Fabric; NeoForge: hasPermission(2)
```

## Multiple config files

A mod with more than one config (see
[`configFor(MOD_ID, "engines")`](getting-started/recommended-structure.md)) gives each its own
subtree under the shared root by passing a **label**:

```java
ConfigCommands.register(dispatcher, MOD_ID, config, feedback);                   // /examplemod config ...
ConfigCommands.register(dispatcher, MOD_ID, "engines", engineConfig, feedback);  // /examplemod engines ...
```

Brigadier merges the shared `examplemod` root, so both coexist:

```text
/examplemod config  list | get <key> | set <key> <value> | reload
/examplemod engines list | get <key> | set <key> <value> | reload
```

Give each config a distinct label — two under the same label would collide.

## Loader wiring

The registration call lives in each loader's command event — the only loader-specific glue. The
exact feedback signature varies by version; adapt the callback to yours.

> [!WARNING]
> These `register(...)` snippets add **no permission check** — `set` and `reload` mutate your
> config. On a shared or public server, gate the commands as shown in
> [Restricting who can change config](#restricting-who-can-change-config).

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

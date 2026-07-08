package com.indemnity83.configory;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.indemnity83.configory.command.ConfigCommands;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConfigCommandsTest {

    enum Mode {
        OFF,
        ON,
        AUTO
    }

    private final Object source = new Object();
    private InMemoryConfigStorage storage;
    private Config config;
    private CommandDispatcher<Object> dispatcher;
    private List<String> feedback;

    @BeforeEach
    void setUp() {
        storage = new InMemoryConfigStorage();
        config = Config.create("examplemod", storage);
        config.defineDouble("engines.max", 10.0).min(0.0).register();
        config.defineInt("core.count", 4).min(0).register();
        config.defineEnum("core.mode", Mode.OFF).register();
        config.defineString("core.secret", "hidden").hidden().register(); // opted out
        config.load();

        feedback = new ArrayList<>();
        dispatcher = new CommandDispatcher<>();
        ConfigCommands.register(dispatcher, "examplemod", config, (src, msg) -> feedback.add(msg));
    }

    private int run(String command) throws CommandSyntaxException {
        return dispatcher.execute(command, source);
    }

    @Test
    void keyWithValueSets() throws CommandSyntaxException {
        run("examplemod config engines.max 5.5");

        assertEquals(5.5, config.get("engines.max").asDouble());
        assertTrue(feedback.stream().anyMatch(m -> m.contains("engines.max") && m.contains("5.5")));
    }

    @Test
    void setRejectsOutOfRangeValueAndLeavesConfigUnchanged() throws CommandSyntaxException {
        run("examplemod config engines.max -1.0");

        assertEquals(10.0, config.get("engines.max").asDouble());
        assertTrue(feedback.stream().anyMatch(m -> m.startsWith("Rejected")));
    }

    @Test
    void keyWithoutValueReportsCurrentValue() throws CommandSyntaxException {
        run("examplemod config core.count");
        assertTrue(feedback.stream().anyMatch(m -> m.contains("core.count") && m.contains("4")));
    }

    @Test
    void bareConfigListsOnlyExposedKeys() throws CommandSyntaxException {
        int count = run("examplemod config");

        assertEquals(3, count, "3 exposed keys; the unexposed secret is excluded");
        assertTrue(feedback.stream().noneMatch(m -> m.contains("core.secret")));
    }

    @Test
    void enumSetAcceptsAConstantAndRejectsUnknown() throws CommandSyntaxException {
        run("examplemod config core.mode AUTO");
        assertEquals(Mode.AUTO, config.get("core.mode").asEnum(Mode.class));

        run("examplemod config core.mode SIDEWAYS");
        assertEquals(Mode.AUTO, config.get("core.mode").asEnum(Mode.class), "unknown constant is rejected");
    }

    @Test
    void hiddenKeyHasNoGetOrSetCommand() {
        assertThrows(CommandSyntaxException.class, () -> run("examplemod config core.secret"));
        assertThrows(CommandSyntaxException.class, () -> run("examplemod config core.secret x"));
    }

    @Test
    void reloadConfigsRefreshesFromDisk() throws CommandSyntaxException {
        config.save(); // clear the dirty state from applied defaults so reload() is allowed

        seed(storage, "examplemod", "engines.max", new JsonPrimitive(6.0));
        run("examplemod reload-configs");

        assertEquals(6.0, config.get("engines.max").asDouble(), "reload picked up the external change");
        assertTrue(feedback.stream().anyMatch(m -> m.contains("Reloaded")));
    }

    @Test
    void mergesWithSiblingCommandsUnderTheSameRoot() throws CommandSyntaxException {
        dispatcher.register(LiteralArgumentBuilder.<Object>literal("examplemod")
                .then(LiteralArgumentBuilder.<Object>literal("ping").executes(c -> {
                    feedback.add("pong");
                    return 1;
                })));

        run("examplemod ping");
        assertTrue(feedback.contains("pong"), "sibling command coexists");

        run("examplemod config core.count");
        assertTrue(feedback.stream().anyMatch(m -> m.contains("core.count")), "config commands still work");
    }

    @Test
    void groupsNestUnderConfigWithNameDerivedFromId() throws CommandSyntaxException {
        Config engines = Config.create("examplemod.engines", new InMemoryConfigStorage());
        engines.defineDouble("stirling.min", 3.0).min(0.0).register();
        engines.load();

        dispatcher = new CommandDispatcher<>();
        feedback.clear();
        ConfigCommands.<Object>forRoot("examplemod", (src, msg) -> feedback.add(msg))
                .add(config)
                .group(engines) // name derived from id "examplemod.engines" -> "engines"
                .register(dispatcher);

        // main keys are native under /examplemod config
        run("examplemod config core.count");
        assertTrue(feedback.stream().anyMatch(m -> m.contains("core.count")));

        // the group nests one layer deeper
        run("examplemod config engines stirling.min 5.0");
        assertEquals(5.0, engines.get("stirling.min").asDouble());
    }

    @Test
    void reloadConfigsReloadsMainAndGroups() throws CommandSyntaxException {
        InMemoryConfigStorage engineStorage = new InMemoryConfigStorage();
        Config engines = Config.create("examplemod.engines", engineStorage);
        engines.defineDouble("stirling.min", 3.0).min(0.0).register();
        engines.load();

        dispatcher = new CommandDispatcher<>();
        feedback.clear();
        ConfigCommands.<Object>forRoot("examplemod", (src, msg) -> feedback.add(msg))
                .add(config)
                .group("engines", engines)
                .register(dispatcher);

        config.save();
        engines.save();
        seed(storage, "examplemod", "engines.max", new JsonPrimitive(6.0));
        seed(engineStorage, "examplemod.engines", "stirling.min", new JsonPrimitive(7.0));

        run("examplemod reload-configs");

        assertEquals(6.0, config.get("engines.max").asDouble(), "main config reloaded");
        assertEquals(7.0, engines.get("stirling.min").asDouble(), "group config reloaded");
    }

    @Test
    void groupsAreSeparateNamespacesSoKeysCanBeReused() throws CommandSyntaxException {
        Config engines = Config.create("examplemod.engines", new InMemoryConfigStorage());
        engines.defineInt("limit", 5).min(0).register();
        engines.load();
        Config pumps = Config.create("examplemod.pumps", new InMemoryConfigStorage());
        pumps.defineInt("limit", 8).min(0).register(); // same key path "limit"
        pumps.load();

        dispatcher = new CommandDispatcher<>();
        feedback.clear();
        ConfigCommands.<Object>forRoot("examplemod", (src, msg) -> feedback.add(msg))
                .group(engines)
                .group(pumps)
                .register(dispatcher);

        run("examplemod config engines limit 3");
        run("examplemod config pumps limit 9");

        assertEquals(3, engines.get("limit").asInt());
        assertEquals(9, pumps.get("limit").asInt(), "the reused key path in another group is independent");
    }

    private static void seed(InMemoryConfigStorage storage, String id, String path, JsonPrimitive value) {
        JsonObject document = new JsonObject();
        JsonPaths.set(document, ConfigPath.parse(path), value);
        storage.seed(id, document);
    }
}

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
    void setUpdatesConfigAndReports() throws CommandSyntaxException {
        run("examplemod config set engines.max 5.5");

        assertEquals(5.5, config.get("engines.max").asDouble());
        assertTrue(feedback.stream().anyMatch(m -> m.contains("engines.max") && m.contains("5.5")));
    }

    @Test
    void setRejectsOutOfRangeValueAndLeavesConfigUnchanged() throws CommandSyntaxException {
        run("examplemod config set engines.max -1.0");

        assertEquals(10.0, config.get("engines.max").asDouble());
        assertTrue(feedback.stream().anyMatch(m -> m.startsWith("Rejected")));
    }

    @Test
    void getReportsCurrentValue() throws CommandSyntaxException {
        run("examplemod config get core.count");
        assertTrue(feedback.stream().anyMatch(m -> m.contains("core.count") && m.contains("4")));
    }

    @Test
    void listCoversOnlyExposedKeys() throws CommandSyntaxException {
        int count = run("examplemod config list");

        assertEquals(3, count, "3 exposed keys; the unexposed secret is excluded");
        assertTrue(feedback.stream().noneMatch(m -> m.contains("core.secret")));
    }

    @Test
    void enumSetAcceptsAConstantAndRejectsUnknown() throws CommandSyntaxException {
        run("examplemod config set core.mode AUTO");
        assertEquals(Mode.AUTO, config.get("core.mode").asEnum(Mode.class));

        run("examplemod config set core.mode SIDEWAYS");
        assertEquals(Mode.AUTO, config.get("core.mode").asEnum(Mode.class), "unknown constant is rejected");
    }

    @Test
    void hiddenKeyHasNoSetOrGetCommand() {
        assertThrows(CommandSyntaxException.class, () -> run("examplemod config set core.secret x"));
        assertThrows(CommandSyntaxException.class, () -> run("examplemod config get core.secret"));
    }

    @Test
    void reloadRefreshesFromDisk() throws CommandSyntaxException {
        config.save(); // clear the dirty state from applied defaults so reload() is allowed

        // change the backing source out from under the config, then reload through the command
        JsonObject external = new JsonObject();
        JsonPaths.set(external, ConfigPath.parse("engines.max"), new JsonPrimitive(6.0));
        storage.seed("examplemod", external);

        run("examplemod config reload");

        assertEquals(6.0, config.get("engines.max").asDouble(), "reload picked up the external change");
        assertTrue(feedback.stream().anyMatch(m -> m.contains("Reloaded")));
    }

    @Test
    void configNodeMergesWithSiblingCommandsUnderTheSameRoot() throws CommandSyntaxException {
        dispatcher.register(LiteralArgumentBuilder.<Object>literal("examplemod")
                .then(LiteralArgumentBuilder.<Object>literal("ping").executes(c -> {
                    feedback.add("pong");
                    return 1;
                })));

        run("examplemod ping");
        assertTrue(feedback.contains("pong"), "sibling command coexists");

        run("examplemod config get core.count");
        assertTrue(feedback.stream().anyMatch(m -> m.contains("core.count")), "config commands still work");
    }

    @Test
    void multipleConfigsShareARootUnderDistinctLabels() throws CommandSyntaxException {
        Config engines = Config.create("examplemod.engines", new InMemoryConfigStorage());
        engines.defineDouble("stirling.min", 3.0).min(0.0).register();
        engines.load();
        ConfigCommands.register(dispatcher, "examplemod", "engines", engines, (src, msg) -> feedback.add(msg));

        // the main config's subtree still works...
        run("examplemod config get core.count");
        assertTrue(feedback.stream().anyMatch(m -> m.contains("core.count")));

        // ...and the second config lives under its own label
        run("examplemod engines set stirling.min 5.0");
        assertEquals(5.0, engines.get("stirling.min").asDouble());
    }
}

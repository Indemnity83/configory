package com.indemnity83.configory.command;

import com.indemnity83.configory.Config;
import com.indemnity83.configory.ConfigDefinition;
import com.indemnity83.configory.ConfigException;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Builds a {@code /gamerule}-style Brigadier command surface for a config's
 * {@linkplain Config#exposedDefinitions() exposed} keys:
 *
 * <pre>
 * /&lt;root&gt; config                       list the main config's keys + values
 * /&lt;root&gt; config &lt;key&gt;                 show a value (get)
 * /&lt;root&gt; config &lt;key&gt; &lt;value&gt;         set it (parsed + validated)
 * /&lt;root&gt; config &lt;group&gt; &lt;key&gt; [value]  the same, nested per extra config file
 * /&lt;root&gt; reload-configs               reload every config
 * </pre>
 *
 * <p>This is the one part of Configory that touches Brigadier; the core stays free of it. It is
 * generic over the command source type {@code S} and never calls a Minecraft API — value parsing is
 * Brigadier's, validation is the config's, and feedback goes through a {@link CommandFeedback}. So a
 * single build works across loaders and Minecraft versions; the consumer supplies only the source
 * type, a feedback callback, and the registration into their loader's command event.
 *
 * <pre>{@code
 * // single config:
 * ConfigCommands.register(dispatcher, MOD_ID, config, (src, msg) -> src.sendMessage(msg));
 *
 * // main config + extra files as nested groups, optionally permission-gated:
 * ConfigCommands.forRoot(MOD_ID, feedback)
 *         .main(config)                    // keys native under /<root> config
 *         .group("engines", enginesConfig) // /<root> config engines <key> ...
 *         .requires(src -> src.hasPermissionLevel(2))
 *         .register(dispatcher);
 * }</pre>
 */
public final class ConfigCommands {
    private static final String VALUE = "value";

    private ConfigCommands() {}

    /**
     * Registers {@code /<root> config …} + {@code /<root> reload-configs} for a single config.
     *
     * <p>Shorthand for {@code forRoot(root, feedback).main(config).register(dispatcher)}.
     *
     * @param dispatcher the command dispatcher to register into
     * @param root the root literal (typically the mod id)
     * @param config the config whose exposed keys drive the commands
     * @param feedback delivers result messages to the command source
     * @param <S> the command source type
     */
    public static <S> void register(
            CommandDispatcher<S> dispatcher, String root, Config config, CommandFeedback<S> feedback) {
        forRoot(root, feedback).main(config).register(dispatcher);
    }

    /**
     * {@return a builder for a config command surface rooted at {@code root}}
     *
     * @param root the root literal (typically the mod id)
     * @param feedback delivers result messages to the command source
     * @param <S> the command source type
     */
    public static <S> Builder<S> forRoot(String root, CommandFeedback<S> feedback) {
        return new Builder<>(root, feedback);
    }

    /**
     * Fluent builder for a config command surface: one native {@link #main(Config) main} config plus
     * any number of nested {@link #group(String, Config) groups}, with an optional
     * {@link #requires(Predicate) permission gate}.
     *
     * @param <S> the command source type
     */
    public static final class Builder<S> {
        private final String root;
        private final CommandFeedback<S> feedback;
        private final Map<String, Config> groups = new LinkedHashMap<>();
        private Config main;
        private Predicate<S> requirement;

        private Builder(String root, CommandFeedback<S> feedback) {
            this.root = root;
            this.feedback = feedback;
        }

        /**
         * Sets the config whose keys appear natively under {@code /<root> config}.
         *
         * @param config the main config
         * @return this builder
         */
        public Builder<S> main(Config config) {
            this.main = config;
            return this;
        }

        /**
         * Adds a config as a group nested under {@code /<root> config <name> …}.
         *
         * @param name the group literal
         * @param config the config for this group
         * @return this builder
         */
        public Builder<S> group(String name, Config config) {
            groups.put(name, config);
            return this;
        }

        /**
         * Adds a config as a group named by the last dot-segment of its {@linkplain Config#id() id}
         * (so {@code "examplemod.engines"} groups under {@code engines}).
         *
         * @param config the config for this group
         * @return this builder
         */
        public Builder<S> group(Config config) {
            return group(lastSegment(config.id()), config);
        }

        /**
         * Restricts the generated {@code config} and {@code reload-configs} commands to sources that
         * satisfy {@code permission} (typically operators). The requirement is applied to those nodes,
         * not the root literal, so the tree still merges with the mod's other {@code /<root> …}
         * commands.
         *
         * @param permission the source predicate to require
         * @return this builder
         */
        public Builder<S> requires(Predicate<S> permission) {
            this.requirement = permission;
            return this;
        }

        /**
         * {@return the root command node with the {@code config} and {@code reload-configs} subtrees}
         *
         * <p>Register it yourself, or nest it, or use {@link #register(CommandDispatcher)}.
         */
        public LiteralArgumentBuilder<S> build() {
            LiteralArgumentBuilder<S> configLit = LiteralArgumentBuilder.literal("config");
            if (main != null) {
                configLit.executes(ctx -> listKeys(ctx, main));
                addKeys(configLit, main);
            }
            for (Map.Entry<String, Config> entry : groups.entrySet()) {
                configLit.then(keyedNode(entry.getKey(), entry.getValue()));
            }
            LiteralArgumentBuilder<S> reload = reloadConfigsNode();
            if (requirement != null) {
                configLit.requires(requirement);
                reload.requires(requirement);
            }
            return LiteralArgumentBuilder.<S>literal(root).then(configLit).then(reload);
        }

        /**
         * Builds and registers the command surface into {@code dispatcher}.
         *
         * @param dispatcher the command dispatcher to register into
         */
        public void register(CommandDispatcher<S> dispatcher) {
            dispatcher.register(build());
        }

        private LiteralArgumentBuilder<S> keyedNode(String label, Config config) {
            LiteralArgumentBuilder<S> node =
                    LiteralArgumentBuilder.<S>literal(label).executes(ctx -> listKeys(ctx, config));
            addKeys(node, config);
            return node;
        }

        private void addKeys(LiteralArgumentBuilder<S> node, Config config) {
            for (ConfigDefinition<?> definition : config.exposedDefinitions()) {
                node.then(LiteralArgumentBuilder.<S>literal(definition.path().fullPath())
                        .executes(ctx -> {
                            feedback.send(ctx.getSource(), line(config, definition));
                            return 1;
                        })
                        .then(valueArgument(config, definition)));
            }
        }

        private LiteralArgumentBuilder<S> reloadConfigsNode() {
            return LiteralArgumentBuilder.<S>literal("reload-configs").executes(ctx -> {
                int reloaded = 0;
                for (Config config : allConfigs()) {
                    try {
                        config.reload();
                        feedback.send(ctx.getSource(), "Reloaded config '" + config.id() + "'.");
                        reloaded++;
                    } catch (ConfigException e) {
                        feedback.send(ctx.getSource(), "Cannot reload config '" + config.id() + "': " + e.getMessage());
                    }
                }
                return reloaded;
            });
        }

        private List<Config> allConfigs() {
            List<Config> all = new ArrayList<>();
            if (main != null) {
                all.add(main);
            }
            all.addAll(groups.values());
            return all;
        }

        private int listKeys(CommandContext<S> ctx, Config config) {
            int count = 0;
            for (ConfigDefinition<?> definition : config.exposedDefinitions()) {
                feedback.send(ctx.getSource(), line(config, definition));
                count++;
            }
            return count;
        }

        private ArgumentBuilder<S, ?> valueArgument(Config config, ConfigDefinition<?> definition) {
            String path = definition.path().fullPath();
            return switch (definition.type()) {
                case BOOLEAN ->
                    RequiredArgumentBuilder.<S, Boolean>argument(VALUE, BoolArgumentType.bool())
                            .executes(ctx -> apply(ctx, config, path, BoolArgumentType.getBool(ctx, VALUE)));
                case INT ->
                    RequiredArgumentBuilder.<S, Integer>argument(VALUE, IntegerArgumentType.integer())
                            .executes(ctx -> apply(ctx, config, path, IntegerArgumentType.getInteger(ctx, VALUE)));
                case LONG ->
                    RequiredArgumentBuilder.<S, Long>argument(VALUE, LongArgumentType.longArg())
                            .executes(ctx -> apply(ctx, config, path, LongArgumentType.getLong(ctx, VALUE)));
                case FLOAT ->
                    RequiredArgumentBuilder.<S, Float>argument(VALUE, FloatArgumentType.floatArg())
                            .executes(ctx -> apply(ctx, config, path, FloatArgumentType.getFloat(ctx, VALUE)));
                case DOUBLE ->
                    RequiredArgumentBuilder.<S, Double>argument(VALUE, DoubleArgumentType.doubleArg())
                            .executes(ctx -> apply(ctx, config, path, DoubleArgumentType.getDouble(ctx, VALUE)));
                case STRING ->
                    RequiredArgumentBuilder.<S, String>argument(VALUE, StringArgumentType.string())
                            .executes(ctx -> apply(ctx, config, path, StringArgumentType.getString(ctx, VALUE)));
                case ENUM ->
                    RequiredArgumentBuilder.<S, String>argument(VALUE, StringArgumentType.string())
                            .suggests(enumSuggestions(definition))
                            .executes(ctx -> applyEnum(ctx, config, definition));
            };
        }

        private int apply(CommandContext<S> ctx, Config config, String path, Object value) {
            if (config.trySet(path, value)) {
                config.save();
                feedback.send(ctx.getSource(), path + " = " + config.get(path).asDisplayString());
                return 1;
            }
            feedback.send(ctx.getSource(), "Rejected value for " + path + ": " + value);
            return 0;
        }

        private <E extends Enum<E>> int applyEnum(
                CommandContext<S> ctx, Config config, ConfigDefinition<?> definition) {
            String raw = StringArgumentType.getString(ctx, VALUE);
            @SuppressWarnings("unchecked")
            Class<E> enumClass = (Class<E>) definition.valueClass();
            E value;
            try {
                value = Enum.valueOf(enumClass, raw);
            } catch (IllegalArgumentException e) {
                feedback.send(
                        ctx.getSource(),
                        "Rejected value for " + definition.path().fullPath() + ": " + raw);
                return 0;
            }
            return apply(ctx, config, definition.path().fullPath(), value);
        }

        private String line(Config config, ConfigDefinition<?> definition) {
            String path = definition.path().fullPath();
            return path + " = " + config.get(path).asDisplayString();
        }
    }

    private static <S> SuggestionProvider<S> enumSuggestions(ConfigDefinition<?> definition) {
        return (ctx, builder) -> {
            for (Object constant : definition.valueClass().getEnumConstants()) {
                builder.suggest(((Enum<?>) constant).name());
            }
            return builder.buildFuture();
        };
    }

    private static String lastSegment(String id) {
        int dot = id.lastIndexOf('.');
        return dot < 0 ? id : id.substring(dot + 1);
    }
}

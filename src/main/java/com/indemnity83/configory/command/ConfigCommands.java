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

/**
 * Builds a Brigadier command surface for a config's {@linkplain Config#exposedDefinitions() exposed}
 * keys: {@code list}, {@code get <key>}, {@code set <key> <value>}, and {@code reload}.
 *
 * <p>This is the one part of Configory that touches Brigadier; the core stays free of it. It is
 * generic over the command source type {@code S} and never calls a Minecraft API — value parsing is
 * Brigadier's, validation is the config's, and feedback goes through a {@link CommandFeedback}. So a
 * single build works across loaders and Minecraft versions; the consumer supplies only the source
 * type, a feedback callback, and the registration into their loader's command event.
 *
 * <pre>{@code
 * // convenience: /examplemod config list|get|set|reload  (merges with your other /examplemod commands)
 * ConfigCommands.register(dispatcher, MOD_ID, config, (src, msg) -> src.sendMessage(msg));
 *
 * // or take the node and place it yourself (add .requires(...), nest under an existing root, ...)
 * dispatcher.register(literal(MOD_ID).then(ConfigCommands.configNode(config, feedback)));
 * }</pre>
 */
public final class ConfigCommands {
    private static final String VALUE = "value";

    private ConfigCommands() {}

    /**
     * Registers {@code <root> config …} into the dispatcher for the config's exposed keys.
     *
     * <p>Brigadier merges command trees that share a root literal, so this coexists with any other
     * {@code <root> …} commands the mod registers, as long as none also claims a {@code config} child.
     *
     * @param dispatcher the command dispatcher to register into
     * @param root the root literal (typically the mod id)
     * @param config the config whose exposed keys drive the commands
     * @param feedback delivers result messages to the command source
     * @param <S> the command source type
     */
    public static <S> void register(
            CommandDispatcher<S> dispatcher, String root, Config config, CommandFeedback<S> feedback) {
        dispatcher.register(LiteralArgumentBuilder.<S>literal(root).then(configNode(config, feedback)));
    }

    /**
     * {@return a {@code config} command node with {@code list}/{@code get}/{@code set}/{@code reload}
     * for the config's exposed keys}
     *
     * <p>Attach it wherever you like — under your mod's root, standalone, or with a
     * {@code .requires(...)} permission wrapper — then register it with your dispatcher.
     *
     * @param config the config whose exposed keys drive the commands
     * @param feedback delivers result messages to the command source
     * @param <S> the command source type
     */
    public static <S> LiteralArgumentBuilder<S> configNode(Config config, CommandFeedback<S> feedback) {
        return LiteralArgumentBuilder.<S>literal("config")
                .then(listNode(config, feedback))
                .then(getNode(config, feedback))
                .then(setNode(config, feedback))
                .then(reloadNode(config, feedback));
    }

    private static <S> LiteralArgumentBuilder<S> listNode(Config config, CommandFeedback<S> feedback) {
        return LiteralArgumentBuilder.<S>literal("list").executes(ctx -> {
            int count = 0;
            for (ConfigDefinition<?> definition : config.exposedDefinitions()) {
                feedback.send(ctx.getSource(), line(config, definition));
                count++;
            }
            return count;
        });
    }

    private static <S> LiteralArgumentBuilder<S> getNode(Config config, CommandFeedback<S> feedback) {
        LiteralArgumentBuilder<S> get = LiteralArgumentBuilder.literal("get");
        for (ConfigDefinition<?> definition : config.exposedDefinitions()) {
            get.then(LiteralArgumentBuilder.<S>literal(definition.path().fullPath())
                    .executes(ctx -> {
                        feedback.send(ctx.getSource(), line(config, definition));
                        return 1;
                    }));
        }
        return get;
    }

    private static <S> LiteralArgumentBuilder<S> setNode(Config config, CommandFeedback<S> feedback) {
        LiteralArgumentBuilder<S> set = LiteralArgumentBuilder.literal("set");
        for (ConfigDefinition<?> definition : config.exposedDefinitions()) {
            set.then(LiteralArgumentBuilder.<S>literal(definition.path().fullPath())
                    .then(valueArgument(config, definition, feedback)));
        }
        return set;
    }

    private static <S> ArgumentBuilder<S, ?> valueArgument(
            Config config, ConfigDefinition<?> definition, CommandFeedback<S> feedback) {
        String path = definition.path().fullPath();
        switch (definition.type()) {
            case BOOLEAN -> {
                return RequiredArgumentBuilder.<S, Boolean>argument(VALUE, BoolArgumentType.bool())
                        .executes(ctx -> apply(ctx, config, feedback, path, BoolArgumentType.getBool(ctx, VALUE)));
            }
            case INT -> {
                return RequiredArgumentBuilder.<S, Integer>argument(VALUE, IntegerArgumentType.integer())
                        .executes(
                                ctx -> apply(ctx, config, feedback, path, IntegerArgumentType.getInteger(ctx, VALUE)));
            }
            case LONG -> {
                return RequiredArgumentBuilder.<S, Long>argument(VALUE, LongArgumentType.longArg())
                        .executes(ctx -> apply(ctx, config, feedback, path, LongArgumentType.getLong(ctx, VALUE)));
            }
            case FLOAT -> {
                return RequiredArgumentBuilder.<S, Float>argument(VALUE, FloatArgumentType.floatArg())
                        .executes(ctx -> apply(ctx, config, feedback, path, FloatArgumentType.getFloat(ctx, VALUE)));
            }
            case DOUBLE -> {
                return RequiredArgumentBuilder.<S, Double>argument(VALUE, DoubleArgumentType.doubleArg())
                        .executes(ctx -> apply(ctx, config, feedback, path, DoubleArgumentType.getDouble(ctx, VALUE)));
            }
            case STRING -> {
                return RequiredArgumentBuilder.<S, String>argument(VALUE, StringArgumentType.string())
                        .executes(ctx -> apply(ctx, config, feedback, path, StringArgumentType.getString(ctx, VALUE)));
            }
            case ENUM -> {
                return RequiredArgumentBuilder.<S, String>argument(VALUE, StringArgumentType.string())
                        .suggests(enumSuggestions(definition))
                        .executes(ctx -> applyEnum(ctx, config, feedback, definition));
            }
        }
        throw new IllegalStateException("Unsupported config type: " + definition.type());
    }

    private static <S> LiteralArgumentBuilder<S> reloadNode(Config config, CommandFeedback<S> feedback) {
        return LiteralArgumentBuilder.<S>literal("reload").executes(ctx -> {
            try {
                config.reload();
                feedback.send(ctx.getSource(), "Reloaded config '" + config.id() + "'.");
                return 1;
            } catch (ConfigException e) {
                feedback.send(ctx.getSource(), "Cannot reload config '" + config.id() + "': " + e.getMessage());
                return 0;
            }
        });
    }

    private static <S> int apply(
            CommandContext<S> ctx, Config config, CommandFeedback<S> feedback, String path, Object value) {
        if (config.trySet(path, value)) {
            config.save();
            feedback.send(ctx.getSource(), path + " = " + config.get(path).asDisplayString());
            return 1;
        }
        feedback.send(ctx.getSource(), "Rejected value for " + path + ": " + value);
        return 0;
    }

    private static <S, E extends Enum<E>> int applyEnum(
            CommandContext<S> ctx, Config config, CommandFeedback<S> feedback, ConfigDefinition<?> definition) {
        String raw = StringArgumentType.getString(ctx, VALUE);
        @SuppressWarnings("unchecked")
        Class<E> enumClass = (Class<E>) definition.valueClass();
        E value;
        try {
            value = Enum.valueOf(enumClass, raw);
        } catch (IllegalArgumentException e) {
            feedback.send(
                    ctx.getSource(), "Rejected value for " + definition.path().fullPath() + ": " + raw);
            return 0;
        }
        return apply(ctx, config, feedback, definition.path().fullPath(), value);
    }

    private static <S> SuggestionProvider<S> enumSuggestions(ConfigDefinition<?> definition) {
        return (ctx, builder) -> {
            for (Object constant : definition.valueClass().getEnumConstants()) {
                builder.suggest(((Enum<?>) constant).name());
            }
            return builder.buildFuture();
        };
    }

    private static String line(Config config, ConfigDefinition<?> definition) {
        String path = definition.path().fullPath();
        return path + " = " + config.get(path).asDisplayString();
    }
}

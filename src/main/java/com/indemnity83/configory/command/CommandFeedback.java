package com.indemnity83.configory.command;

/**
 * Sends a message back to a command source.
 *
 * <p>The generated command handlers report results (a value listing, a successful set, a rejected
 * value) through this callback rather than calling any Minecraft API directly. That is what keeps
 * {@link ConfigCommands} loader- and version-agnostic: the consumer supplies the one line that turns
 * a string into their platform's chat feedback.
 *
 * @param <S> the command source type (e.g. a Fabric {@code ServerCommandSource} or a NeoForge
 *     {@code CommandSourceStack})
 */
@FunctionalInterface
public interface CommandFeedback<S> {
    /**
     * Sends {@code message} to {@code source}.
     *
     * @param source the command source that ran the command
     * @param message the human-readable message to deliver
     */
    void send(S source, String message);
}

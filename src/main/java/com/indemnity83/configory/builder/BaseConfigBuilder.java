package com.indemnity83.configory.builder;

import com.indemnity83.configory.*;
import com.indemnity83.configory.ConfigDefinition.FormerSource;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Shared fluent builder logic common to every value type.
 *
 * <p>Provides the default value, description, and custom validator steps, plus the terminal
 * {@link #register()} that produces a {@link ConfigKey}. Uses a self-type so type-specific
 * subclasses (such as numeric or string builders) keep returning their own concrete type from
 * chained calls.
 *
 * @param <T> the value type
 * @param <SELF> the concrete builder subtype, for fluent chaining
 */
public abstract class BaseConfigBuilder<T, SELF extends BaseConfigBuilder<T, SELF>> {
    protected final Config config;
    protected final ConfigPath path;
    protected final ConfigType type;
    protected final Class<T> valueClass;
    protected T defaultValue;
    protected String description = "";
    protected boolean exposed = true;
    protected final List<ConfigConstraint<T>> constraints = new ArrayList<>();
    protected final List<FormerSource<T>> formerSources = new ArrayList<>();

    /**
     * Creates a builder for a specific type.
     *
     * @param config the config the resulting key will belong to
     * @param path the path being defined
     * @param type the config type
     * @param valueClass the Java class of the value
     */
    protected BaseConfigBuilder(Config config, ConfigPath path, ConfigType type, Class<T> valueClass) {
        this.config = config;
        this.path = path;
        this.type = type;
        this.valueClass = valueClass;
    }

    /**
     * {@return this builder as its concrete subtype, for fluent chaining}
     */
    protected abstract SELF self();

    /**
     * Sets the default value used when the key is unset or a stored value is invalid.
     *
     * <p>A default is required before {@link #register()}.
     *
     * @param value the default value
     * @return this builder
     */
    public SELF defaultValue(T value) {
        this.defaultValue = value;
        return self();
    }

    /**
     * Alias for {@link #defaultValue(Object)}.
     *
     * @param value the default value
     * @return this builder
     */
    public SELF defaultsTo(T value) {
        return defaultValue(value);
    }

    /**
     * Sets a human-readable description for the value, surfaced to tooling (commands, generated docs,
     * config screens).
     *
     * @param description the description text
     * @return this builder
     */
    public SELF describe(String description) {
        this.description = description;
        return self();
    }

    /**
     * Adds a custom validation constraint, run in order alongside any built-in constraints.
     *
     * @param constraint the constraint to add
     * @return this builder
     */
    public SELF validator(ConfigConstraint<T> constraint) {
        this.constraints.add(constraint);
        return self();
    }

    /**
     * Adds a former path this key was stored at, so a renamed key migrates its old value.
     *
     * <p>On load, when the primary path holds no value, declared sources are tried in the order added;
     * the first present value that coerces and validates is adopted at the primary path. Every former
     * path is then stripped from the document, so the rename settles on the next {@code save()}.
     * Repeatable, and mixable with {@link #formerly(Supplier)}, to declare more than one source.
     *
     * @param path the former dotted path, e.g. {@code "core.old_speed"}
     * @return this builder
     */
    public SELF formerly(String path) {
        this.formerSources.add(FormerSource.ofPath(ConfigPath.parse(path)));
        return self();
    }

    /**
     * Adds a supplier that produces this key's value when its primary path is unset — for migrating
     * from a source Configory does not manage, such as another config or a computed value.
     *
     * <p>On load, when the primary path holds no value, declared sources are tried in the order added.
     * The supplier is called only then; a returned value that passes validation is adopted at the
     * primary path and persisted on the next {@code save()}, so the supplier runs once. A supplier
     * that returns {@code null} or throws is skipped, falling through to the next source or the
     * default. Repeatable, and mixable with {@link #formerly(String)}.
     *
     * @param value produces the value to migrate in, or {@code null} to contribute nothing
     * @return this builder
     */
    public SELF formerly(Supplier<T> value) {
        this.formerSources.add(FormerSource.ofSupplier(value));
        return self();
    }

    /**
     * Excludes this value from the generated command surface.
     *
     * <p>Opt-out: every key appears in the {@code list}/{@code get}/{@code set} commands built by
     * {@code ConfigCommands} by default; call this to keep one out — a setting driven by its own
     * command, or one players shouldn't change.
     *
     * @return this builder
     */
    public SELF hidden() {
        this.exposed = false;
        return self();
    }

    /**
     * Builds the definition, registers it with the config, and returns the resulting typed key.
     *
     * @return a {@link ConfigKey} for the newly registered value
     * @throws ConfigException if no default value was set, or a definition already exists at this path
     */
    public ConfigKey<T> register() {
        if (defaultValue == null) {
            throw new ConfigException("Config key " + path.fullPath() + " is missing a default value.");
        }
        ConfigDefinition<T> definition = new ConfigDefinition<>(
                path, type, valueClass, defaultValue, description, exposed, constraints, formerSources);
        return config.registerDefinition(definition);
    }
}

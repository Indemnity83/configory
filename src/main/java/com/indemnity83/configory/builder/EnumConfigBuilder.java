package com.indemnity83.configory.builder;

import com.indemnity83.configory.Config;
import com.indemnity83.configory.ConfigPath;
import com.indemnity83.configory.ConfigType;

/**
 * Fluent builder for an enum-typed config value, stored by the constant's {@code name()}.
 *
 * <p>Obtained via {@code define(path).asEnum(MyEnum.class)}. An unknown or wrongly-cased name in the
 * file fails coercion and is repaired to the default on load, exactly like any other invalid value.
 *
 * @param <E> the enum type
 */
public final class EnumConfigBuilder<E extends Enum<E>> extends BaseConfigBuilder<E, EnumConfigBuilder<E>> {
    /**
     * Creates an enum builder. Normally obtained via {@code define(path).asEnum(enumClass)}.
     *
     * @param config the config the resulting key will belong to
     * @param path the path being defined
     * @param enumClass the enum type
     */
    public EnumConfigBuilder(Config config, ConfigPath path, Class<E> enumClass) {
        super(config, path, ConfigType.ENUM, enumClass);
    }

    @Override
    protected EnumConfigBuilder<E> self() {
        return this;
    }
}

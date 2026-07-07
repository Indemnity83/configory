package com.indemnity83.configory;

/**
 * A stand-in for a consuming mod, used to exercise the reflection-driven bootstrap magic
 * (host -> nested {@code Configs} -> registry -> load).
 */
public final class BootstrapTestMod implements ConfigHost {
    public static final String MOD_ID = "configory_it";

    public static final class Configs extends ConfigEntries {
        private static final Config config = configFor(MOD_ID);
        private static final Config engines = configFor(MOD_ID, "engines");

        private Configs() {}

        public static final ConfigKey<Float> SPEED = config.define("core.speed")
                .asFloat()
                .defaultValue(1.0f)
                .range(0.1f, 10.0f)
                .describe("Global speed multiplier.")
                .register();

        public static final ConfigKey<Integer> COUNT =
                config.define("core.count").asInt().defaultValue(4).min(0).register();

        public static final ConfigKey<Double> STIRLING_MIN = engines.define("stirling.min_output")
                .asDouble()
                .defaultValue(3.0)
                .register();

        static boolean bootstrapRan = false;

        public static void bootstrap(Config config) {
            bootstrapRan = true;
        }
    }
}

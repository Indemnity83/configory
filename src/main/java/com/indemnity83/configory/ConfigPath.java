package com.indemnity83.configory;

import java.util.List;
import java.util.Objects;

/**
 * A parsed config path: the dotted key segments that traverse the JSON tree within a config's file.
 *
 * <p>Every dot is a nesting boundary, so {@code "engines.stirling.min_output"} addresses
 * {@code engines -> stirling -> min_output} inside the config's document. Which file a config lives
 * in is decided by the {@linkplain Config#id() config id}, not by the path.
 *
 * @param segments the value segments; never empty and containing no blank entries
 */
public record ConfigPath(List<String> segments) {
    /**
     * Canonical constructor that validates and defensively copies the segments.
     *
     * @throws NullPointerException if {@code segments} is null
     * @throws ConfigException if there are no segments, or any segment is blank
     */
    public ConfigPath {
        Objects.requireNonNull(segments, "segments");
        if (segments.isEmpty()) {
            throw new ConfigException("Config path must include at least one segment.");
        }
        for (String segment : segments) {
            if (segment == null || segment.isBlank()) {
                throw new ConfigException("Config path contains a blank segment.");
            }
        }
        segments = List.copyOf(segments);
    }

    /**
     * Parses a dotted path string into its nested key segments.
     *
     * <p>Every dot is a nesting boundary, so {@code "core.speed_multiplier"} nests
     * {@code speed_multiplier} under {@code core}, and a dot-less string is a single top-level key.
     *
     * @param raw the path string, e.g. {@code "core.speed_multiplier"} or {@code "speed_multiplier"}
     * @return the parsed path
     * @throws NullPointerException if {@code raw} is null
     * @throws ConfigException if the path contains an empty segment (e.g. a leading, trailing, or
     *     doubled dot)
     */
    public static ConfigPath parse(String raw) {
        Objects.requireNonNull(raw, "raw");
        int keepTrailingEmptyStrings = -1;
        String[] parts = raw.split("\\.", keepTrailingEmptyStrings);
        for (String part : parts) {
            if (part.isBlank()) {
                throw new ConfigException("Config path contains an empty segment: " + raw);
            }
        }
        return new ConfigPath(List.of(parts));
    }

    /**
     * {@return the dotted form {@code segment[.segment...]}}
     */
    public String fullPath() {
        return String.join(".", segments);
    }

    /**
     * {@return the {@linkplain #fullPath() dotted path}}
     */
    @Override
    public String toString() {
        return fullPath();
    }
}

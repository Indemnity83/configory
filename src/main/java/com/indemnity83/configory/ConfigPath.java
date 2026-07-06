package com.indemnity83.configory;

import java.util.List;
import java.util.Objects;

/**
 * A parsed config path: the file it lives in plus the segments that traverse the JSON tree within
 * that file.
 *
 * <p>Paths are dotted: the first segment is the file (without the {@code .json} extension) and the
 * rest walk into nested JSON objects. So {@code "engines.stirling.min_output"} targets
 * {@code engines.json} at {@code stirling -> min_output}. A path with no dot is a bare key and lands
 * in the reserved default file (see {@link #DEFAULT_FILE}), so {@code "speed_multiplier"} and the
 * explicit alias {@code "config.speed_multiplier"} refer to the same value in {@code config.json}.
 *
 * @param file the file name (first segment), without the {@code .json} extension
 * @param segments the value segments within the file; never empty and containing no blank entries
 */
public record ConfigPath(String file, List<String> segments) {
    /**
     * The reserved default file name. A path with no {@code .} (a bare key) lands here, so
     * {@code "speed_multiplier"} and the explicit alias {@code "config.speed_multiplier"} both
     * resolve to {@code config.json}.
     */
    public static final String DEFAULT_FILE = "config";

    /**
     * Canonical constructor that validates and defensively copies the segments.
     *
     * @throws NullPointerException if {@code file} or {@code segments} is null
     * @throws ConfigException if the file is blank, there are no segments, or any segment is blank
     */
    public ConfigPath {
        Objects.requireNonNull(file, "file");
        Objects.requireNonNull(segments, "segments");
        if (file.isBlank()) {
            throw new ConfigException("Config path file segment cannot be blank.");
        }
        if (segments.isEmpty()) {
            throw new ConfigException("Config path must include a file and at least one value segment.");
        }
        for (String segment : segments) {
            if (segment == null || segment.isBlank()) {
                throw new ConfigException("Config path contains a blank segment.");
            }
        }
        segments = List.copyOf(segments);
    }

    /**
     * Parses a dotted path string.
     *
     * <p>A single segment (no dot) becomes a bare key in {@link #DEFAULT_FILE}; otherwise the first
     * segment is the file and the rest are the value segments.
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
        boolean bareKey = parts.length == 1;
        if (bareKey) {
            return new ConfigPath(DEFAULT_FILE, List.of(parts[0]));
        }
        return new ConfigPath(parts[0], List.of(parts).subList(1, parts.length));
    }

    /**
     * {@return the fully qualified dotted form {@code file.segment[.segment...]}}
     *
     * <p>Bare keys render in their explicit {@code config.<key>} form, so the backing file is never
     * ambiguous in logs and error messages.
     */
    public String fullPath() {
        return file + "." + String.join(".", segments);
    }

    /**
     * {@return the {@linkplain #fullPath() fully qualified path}}
     */
    @Override
    public String toString() {
        return fullPath();
    }
}

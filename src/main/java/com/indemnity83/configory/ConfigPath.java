package com.indemnity83.configory;

import java.util.List;
import java.util.Objects;

public record ConfigPath(String file, List<String> segments) {
    /**
     * The reserved default file name. A path with no {@code .} (a bare key) lands here, so
     * {@code "speed_multiplier"} and the explicit alias {@code "config.speed_multiplier"} both
     * resolve to {@code config.json}.
     */
    public static final String DEFAULT_FILE = "config";

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

    public static ConfigPath parse(String raw) {
        Objects.requireNonNull(raw, "raw");
        // Limit -1 keeps trailing/leading empties so malformed paths like "foo." or ".foo"
        // surface as blank segments instead of being silently dropped.
        String[] parts = raw.split("\\.", -1);
        for (String part : parts) {
            if (part.isBlank()) {
                throw new ConfigException("Config path contains an empty segment: " + raw);
            }
        }
        if (parts.length == 1) {
            // A bare key (no dot) targets the default config file.
            return new ConfigPath(DEFAULT_FILE, List.of(parts[0]));
        }
        return new ConfigPath(parts[0], List.of(parts).subList(1, parts.length));
    }

    public String fullPath() {
        return file + "." + String.join(".", segments);
    }

    @Override
    public String toString() {
        return fullPath();
    }
}

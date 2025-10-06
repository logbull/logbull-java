package com.logbull.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a single log entry to be sent to LogBull.
 */
public class LogEntry {
    private final String level;
    private final String message;
    private final String timestamp;
    private final Map<String, Object> fields;

    public LogEntry(String level, String message, String timestamp, Map<String, Object> fields) {
        this.level = Objects.requireNonNull(level, "level cannot be null");
        this.message = Objects.requireNonNull(message, "message cannot be null");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp cannot be null");
        this.fields = fields != null ? new HashMap<>(fields) : new HashMap<>();
    }

    public String getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getFields() {
        return Collections.unmodifiableMap(fields);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        LogEntry logEntry = (LogEntry) o;
        return Objects.equals(level, logEntry.level) &&
                Objects.equals(message, logEntry.message) &&
                Objects.equals(timestamp, logEntry.timestamp) &&
                Objects.equals(fields, logEntry.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, message, timestamp, fields);
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "level='" + level + '\'' +
                ", message='" + message + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", fields=" + fields +
                '}';
    }
}

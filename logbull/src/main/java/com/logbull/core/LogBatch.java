package com.logbull.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a batch of log entries to be sent to LogBull.
 */
public class LogBatch {
    private final List<LogEntry> logs;

    public LogBatch(List<LogEntry> logs) {
        this.logs = logs != null ? new ArrayList<>(logs) : new ArrayList<>();
    }

    public List<LogEntry> getLogs() {
        return Collections.unmodifiableList(logs);
    }

    public int size() {
        return logs.size();
    }

    public boolean isEmpty() {
        return logs.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        LogBatch logBatch = (LogBatch) o;
        return Objects.equals(logs, logBatch.logs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logs);
    }

    @Override
    public String toString() {
        return "LogBatch{" +
                "logs=" + logs +
                '}';
    }
}

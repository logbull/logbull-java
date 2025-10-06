package com.logbull.core;

/**
 * Log severity levels supported by LogBull.
 */
public enum LogLevel {
    DEBUG(10),
    INFO(20),
    WARNING(30),
    ERROR(40),
    CRITICAL(50);

    private final int priority;

    LogLevel(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public String toString() {
        return name();
    }
}

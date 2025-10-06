package com.logbull.internal.timestamp;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Generates unique, monotonically increasing timestamps with nanosecond
 * precision.
 */
public class TimestampGenerator {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'")
            .withZone(ZoneOffset.UTC);

    private long lastTimestampNanos = 0;
    private final Object lock = new Object();

    public String generateUniqueTimestamp() {
        synchronized (lock) {
            long currentNanos = System.currentTimeMillis() * 1_000_000 + System.nanoTime() % 1_000_000;

            if (currentNanos <= lastTimestampNanos) {
                currentNanos = lastTimestampNanos + 1;
            }

            lastTimestampNanos = currentNanos;
            return formatTimestamp(currentNanos);
        }
    }

    private String formatTimestamp(long timestampNanos) {
        long seconds = timestampNanos / 1_000_000_000;
        long nanos = timestampNanos % 1_000_000_000;
        Instant instant = Instant.ofEpochSecond(seconds, nanos);
        return FORMATTER.format(instant);
    }
}

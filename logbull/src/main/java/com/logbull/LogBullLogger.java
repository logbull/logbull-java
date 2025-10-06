package com.logbull;

import com.logbull.core.LogEntry;
import com.logbull.core.LogLevel;
import com.logbull.internal.formatting.Formatter;
import com.logbull.internal.sender.LogSender;
import com.logbull.internal.timestamp.TimestampGenerator;
import com.logbull.internal.validation.Validator;

import java.util.HashMap;
import java.util.Map;

/**
 * Standalone logger that sends logs to LogBull server.
 */
public class LogBullLogger {
    private final Config config;
    private final LogSender sender;
    private final LogLevel minLevel;
    private final Map<String, Object> context;
    private final Validator validator;
    private final Formatter formatter;
    private final TimestampGenerator timestampGenerator;

    private LogBullLogger(Config config, LogSender sender, Map<String, Object> context) {
        this.config = config;
        this.sender = sender;
        this.minLevel = config.getLogLevel();
        this.context = context != null ? new HashMap<>(context) : new HashMap<>();
        this.validator = new Validator();
        this.formatter = new Formatter();
        this.timestampGenerator = new TimestampGenerator();
    }

    /**
     * Creates a new LogBullLogger with the given configuration.
     *
     * @param config LogBull configuration
     * @return new LogBullLogger instance
     * @throws IllegalArgumentException if configuration is invalid
     */
    public static LogBullLogger create(Config config) {
        Validator validator = new Validator();

        // Validate configuration
        validator.validateProjectId(config.getProjectId());
        validator.validateHostUrl(config.getHost());
        validator.validateApiKey(config.getApiKey());

        LogSender sender = new LogSender(config);
        return new LogBullLogger(config, sender, null);
    }

    /**
     * Builder for creating LogBullLogger instances.
     *
     * @return new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Logs a debug message.
     *
     * @param message log message
     */
    public void debug(String message) {
        debug(message, null);
    }

    /**
     * Logs a debug message with fields.
     *
     * @param message log message
     * @param fields  additional fields
     */
    public void debug(String message, Map<String, Object> fields) {
        log(LogLevel.DEBUG, message, fields);
    }

    /**
     * Logs an info message.
     *
     * @param message log message
     */
    public void info(String message) {
        info(message, null);
    }

    /**
     * Logs an info message with fields.
     *
     * @param message log message
     * @param fields  additional fields
     */
    public void info(String message, Map<String, Object> fields) {
        log(LogLevel.INFO, message, fields);
    }

    /**
     * Logs a warning message.
     *
     * @param message log message
     */
    public void warning(String message) {
        warning(message, null);
    }

    /**
     * Logs a warning message with fields.
     *
     * @param message log message
     * @param fields  additional fields
     */
    public void warning(String message, Map<String, Object> fields) {
        log(LogLevel.WARNING, message, fields);
    }

    /**
     * Logs an error message.
     *
     * @param message log message
     */
    public void error(String message) {
        error(message, null);
    }

    /**
     * Logs an error message with fields.
     *
     * @param message log message
     * @param fields  additional fields
     */
    public void error(String message, Map<String, Object> fields) {
        log(LogLevel.ERROR, message, fields);
    }

    /**
     * Logs a critical message.
     *
     * @param message log message
     */
    public void critical(String message) {
        critical(message, null);
    }

    /**
     * Logs a critical message with fields.
     *
     * @param message log message
     * @param fields  additional fields
     */
    public void critical(String message, Map<String, Object> fields) {
        log(LogLevel.CRITICAL, message, fields);
    }

    /**
     * Creates a new logger instance with additional context fields.
     * The new logger shares the same sender instance.
     *
     * @param context additional context fields
     * @return new logger instance with merged context
     */
    public LogBullLogger withContext(Map<String, Object> context) {
        Map<String, Object> mergedContext = formatter.mergeFields(this.context, context);
        return new LogBullLogger(this.config, this.sender, mergedContext);
    }

    /**
     * Immediately sends all queued logs to LogBull server.
     */
    public void flush() {
        sender.flush();
    }

    /**
     * Stops the logger and sends all remaining logs.
     */
    public void shutdown() {
        sender.shutdown();
    }

    private void log(LogLevel level, String message, Map<String, Object> fields) {
        try {
            // Check log level
            if (level.getPriority() < minLevel.getPriority()) {
                return;
            }

            // Validate inputs
            validator.validateLogMessage(message);
            validator.validateLogFields(fields);

            // Merge context and fields
            Map<String, Object> mergedFields = formatter.mergeFields(context, fields);

            // Format message and fields
            String formattedMessage = formatter.formatMessage(message);
            Map<String, Object> ensuredFields = formatter.ensureFields(mergedFields);

            // Generate unique timestamp
            String timestamp = timestampGenerator.generateUniqueTimestamp();

            // Create log entry
            LogEntry entry = new LogEntry(
                    level.toString(),
                    formattedMessage,
                    timestamp,
                    ensuredFields);

            // Print to console
            printToConsole(entry);

            // Add to send queue
            sender.addLog(entry);

        } catch (Exception e) {
            System.err.println("LogBull: invalid log message: " + e.getMessage());
        }
    }

    private void printToConsole(LogEntry entry) {
        StringBuilder output = new StringBuilder();
        output.append("[").append(entry.getTimestamp()).append("]");
        output.append(" [").append(entry.getLevel()).append("]");
        output.append(" ").append(entry.getMessage());

        if (!entry.getFields().isEmpty()) {
            output.append(" (");
            boolean first = true;
            for (Map.Entry<String, Object> field : entry.getFields().entrySet()) {
                if (!first) {
                    output.append(", ");
                }
                output.append(field.getKey()).append("=").append(field.getValue());
                first = false;
            }
            output.append(")");
        }

        if ("ERROR".equals(entry.getLevel()) || "CRITICAL".equals(entry.getLevel())) {
            System.err.println(output);
        } else {
            System.out.println(output);
        }
    }

    /**
     * Builder for LogBullLogger.
     */
    public static class Builder {
        private String projectId;
        private String host;
        private String apiKey;
        private LogLevel logLevel = LogLevel.INFO;

        private Builder() {
        }

        public Builder projectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder logLevel(LogLevel logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        public LogBullLogger build() {
            Config config = Config.builder()
                    .projectId(projectId)
                    .host(host)
                    .apiKey(apiKey)
                    .logLevel(logLevel)
                    .build();

            return LogBullLogger.create(config);
        }
    }
}

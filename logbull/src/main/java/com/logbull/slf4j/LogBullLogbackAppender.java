package com.logbull.slf4j;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.logbull.Config;
import com.logbull.core.LogEntry;
import com.logbull.core.LogLevel;
import com.logbull.internal.formatting.Formatter;
import com.logbull.internal.sender.LogSender;
import com.logbull.internal.timestamp.TimestampGenerator;
import com.logbull.internal.validation.Validator;

import java.util.HashMap;
import java.util.Map;

/**
 * Logback appender that sends logs to LogBull server.
 */
public class LogBullLogbackAppender extends AppenderBase<ILoggingEvent> {
    private String projectId;
    private String host;
    private String apiKey;
    private String logLevel = "INFO";

    private LogSender sender;
    private LogLevel minLevel;
    private Formatter formatter;
    private TimestampGenerator timestampGenerator;
    private Validator validator;

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    @Override
    public void start() {
        if (projectId == null || projectId.trim().isEmpty()) {
            addError("LogBull projectId is required");
            return;
        }

        if (host == null || host.trim().isEmpty()) {
            addError("LogBull host is required");
            return;
        }

        try {
            validator = new Validator();
            validator.validateProjectId(projectId);
            validator.validateHostUrl(host);
            validator.validateApiKey(apiKey);

            Config config = Config.builder()
                    .projectId(projectId.trim())
                    .host(host.trim())
                    .apiKey(apiKey != null ? apiKey.trim() : null)
                    .logLevel(parseLogLevel(logLevel))
                    .build();

            this.sender = new LogSender(config);
            this.minLevel = config.getLogLevel();
            this.formatter = new Formatter();
            this.timestampGenerator = new TimestampGenerator();

            super.start();
        } catch (Exception e) {
            addError("Failed to initialize LogBull appender: " + e.getMessage(), e);
        }
    }

    @Override
    public void stop() {
        if (sender != null) {
            sender.shutdown();
        }
        super.stop();
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (!isStarted()) {
            return;
        }

        try {
            LogLevel level = convertLogbackLevel(eventObject.getLevel());

            if (level.getPriority() < minLevel.getPriority()) {
                return;
            }

            String message = eventObject.getFormattedMessage();
            Map<String, Object> fields = extractFields(eventObject);

            String formattedMessage = formatter.formatMessage(message);
            Map<String, Object> ensuredFields = formatter.ensureFields(fields);
            String timestamp = timestampGenerator.generateUniqueTimestamp();

            LogEntry entry = new LogEntry(
                    level.toString(),
                    formattedMessage,
                    timestamp,
                    ensuredFields);

            sender.addLog(entry);

        } catch (Exception e) {
            addError("Failed to append log to LogBull: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> extractFields(ILoggingEvent event) {
        Map<String, Object> fields = new HashMap<>();

        // Add MDC properties
        if (event.getMDCPropertyMap() != null) {
            fields.putAll(event.getMDCPropertyMap());
        }

        // Add markers
        if (event.getMarkerList() != null && !event.getMarkerList().isEmpty()) {
            // If there's only one marker, store it as a string
            if (event.getMarkerList().size() == 1) {
                fields.put("marker", event.getMarkerList().get(0).getName());
            } else {
                // If there are multiple markers, store them as a list
                fields.put("marker", event.getMarkerList().stream()
                        .map(marker -> marker.getName())
                        .collect(java.util.stream.Collectors.toList()));
            }
        }

        // Add logger name
        fields.put("logger", event.getLoggerName());

        // Add thread name
        if (event.getThreadName() != null) {
            fields.put("thread", event.getThreadName());
        }

        return fields;
    }

    private LogLevel convertLogbackLevel(Level level) {
        if (level == null) {
            return LogLevel.INFO;
        }

        switch (level.toInt()) {
            case Level.TRACE_INT:
            case Level.DEBUG_INT:
                return LogLevel.DEBUG;
            case Level.INFO_INT:
                return LogLevel.INFO;
            case Level.WARN_INT:
                return LogLevel.WARNING;
            case Level.ERROR_INT:
                return LogLevel.ERROR;
            default:
                return LogLevel.INFO;
        }
    }

    private LogLevel parseLogLevel(String level) {
        if (level == null || level.trim().isEmpty()) {
            return LogLevel.INFO;
        }

        try {
            return LogLevel.valueOf(level.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            addWarn("Invalid log level '" + level + "', defaulting to INFO");
            return LogLevel.INFO;
        }
    }
}

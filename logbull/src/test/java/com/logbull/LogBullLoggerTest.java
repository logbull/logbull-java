package com.logbull;

import com.logbull.core.LogLevel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LogBullLoggerTest {
    private LogBullLogger logger;

    @AfterEach
    void tearDown() {
        if (logger != null) {
            logger.shutdown();
        }
    }

    @Test
    void testCreateLogger() {
        Config config = Config.builder()
                .projectId("12345678-1234-1234-1234-123456789012")
                .host("http://localhost:4005")
                .build();

        logger = LogBullLogger.create(config);
        assertNotNull(logger);
    }

    @Test
    void testBuilderPattern() {
        logger = LogBullLogger.builder()
                .projectId("12345678-1234-1234-1234-123456789012")
                .host("http://localhost:4005")
                .apiKey("test-api-key")
                .logLevel(LogLevel.DEBUG)
                .build();

        assertNotNull(logger);
    }

    @Test
    void testInvalidProjectId() {
        assertThrows(IllegalArgumentException.class, () -> LogBullLogger.builder()
                .projectId("invalid")
                .host("http://localhost:4005")
                .build());
    }

    @Test
    void testInvalidHost() {
        assertThrows(IllegalArgumentException.class, () -> LogBullLogger.builder()
                .projectId("12345678-1234-1234-1234-123456789012")
                .host("invalid")
                .build());
    }

    @Test
    void testLogMethods() {
        logger = LogBullLogger.builder()
                .projectId("12345678-1234-1234-1234-123456789012")
                .host("http://localhost:4005")
                .logLevel(LogLevel.DEBUG)
                .build();

        // Should not throw exceptions
        logger.debug("debug message");
        logger.info("info message");
        logger.warning("warning message");
        logger.error("error message");
        logger.critical("critical message");
    }

    @Test
    void testLogWithFields() {
        logger = LogBullLogger.builder()
                .projectId("12345678-1234-1234-1234-123456789012")
                .host("http://localhost:4005")
                .build();

        Map<String, Object> fields = Map.of(
                "user_id", "12345",
                "action", "login",
                "count", 42);

        logger.info("User action", fields);
    }

    @Test
    void testWithContext() {
        logger = LogBullLogger.builder()
                .projectId("12345678-1234-1234-1234-123456789012")
                .host("http://localhost:4005")
                .build();

        Map<String, Object> context = Map.of(
                "session_id", "sess_123",
                "user_id", "user_456");

        LogBullLogger contextLogger = logger.withContext(context);
        assertNotNull(contextLogger);

        contextLogger.info("Context test");
    }

    @Test
    void testFlush() {
        logger = LogBullLogger.builder()
                .projectId("12345678-1234-1234-1234-123456789012")
                .host("http://localhost:4005")
                .build();

        logger.info("Test message");
        logger.flush();
    }

    @Test
    void testShutdown() {
        logger = LogBullLogger.builder()
                .projectId("12345678-1234-1234-1234-123456789012")
                .host("http://localhost:4005")
                .build();

        logger.info("Test message");
        logger.shutdown();

        // Should not throw exception after shutdown
        logger.info("After shutdown");
    }
}

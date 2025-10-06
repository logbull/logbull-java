# LogBull Java

<div align="center">
<img src="./assets/logo.svg" style="margin-bottom: 20px;" alt="Log Bull Logo" width="250"/>

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.oracle.com/java/)

A Java library for sending logs to [Log Bull](https://github.com/logbull/logbull) - a simple log collection system.

</div>

## Table of Contents

- [Features](#features)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Usage Examples](#usage-examples)
  - [1. Standalone LogBullLogger](#1-standalone-logbulllogger)
  - [2. Spring Boot Integration](#2-spring-boot-integration)
  - [3. SLF4J + Logback Integration](#3-slf4j--logback-integration)
- [Configuration Options](#configuration-options)
  - [Config Parameters](#config-parameters)
  - [Available Log Levels](#available-log-levels)
- [API Reference](#api-reference)
  - [LogBullLogger Methods](#logbulllogger-methods)
  - [Builder Pattern](#builder-pattern)
- [Requirements](#requirements)
- [License](#license)
- [Contributing](#contributing)
- [LogBull Server](#logbull-server)

## Features

- **Multiple integration options**: Standalone logger, Spring Boot starter, and SLF4J/Logback integration
- **Spring Boot auto-configuration**: Zero-configuration setup for Spring Boot applications
- **Context support**: Attach persistent context to logs (session_id, user_id, etc.)
- **Thread-safe**: All operations are safe for concurrent use
- **Asynchronous**: Non-blocking log sending with automatic batching
- **Zero-dependency core**: Only Jackson for JSON serialization

## Installation

### Maven

```xml
<dependency>
    <groupId>com.logbull</groupId>
    <artifactId>logbull</artifactId>
    <version>RELEASE</version>
</dependency>
```

### Maven + Spring Boot

```xml
<dependency>
    <groupId>com.logbull</groupId>
    <artifactId>logbull</artifactId>
    <version>RELEASE</version>
</dependency>

<dependency>
    <groupId>com.logbull</groupId>
    <artifactId>logbull-spring-boot-starter</artifactId>
    <version>RELEASE</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.logbull:logbull:+'
```

### Gradle + Spring Boot

```groovy
implementation 'com.logbull:logbull:+'
implementation 'com.logbull:logbull-spring-boot-starter:+'
```

## Quick Start

The fastest way to start using LogBull is with the standalone logger:

```java
import com.logbull.LogBullLogger;
import com.logbull.core.LogLevel;

import java.util.Map;

public class Application {
    public static void main(String[] args) {
        LogBullLogger logger = LogBullLogger.builder()
            .projectId("12345678-1234-1234-1234-123456789012")
            .host("http://localhost:4005")
            .apiKey("your-api-key")  // optional
            .logLevel(LogLevel.INFO)
            .build();

        try {
            logger.info("User logged in successfully", Map.of(
                "user_id", "12345",
                "username", "john_doe",
                "ip", "192.168.1.100"
            ));
        } finally {
            logger.shutdown();
        }
    }
}
```

## Usage Examples

### 1. Standalone LogBullLogger

#### Basic Usage

```java
import com.logbull.LogBullLogger;
import com.logbull.core.LogLevel;

import java.util.Map;

public class BasicExample {
    public static void main(String[] args) {
        // Create logger with builder pattern
        LogBullLogger logger = LogBullLogger.builder()
                .projectId("12345678-1234-1234-1234-123456789012")
                .host("http://localhost:4005")
                .apiKey("your-api-key") // optional
                .logLevel(LogLevel.INFO)
                .build();

        try {
            // Basic logging
            logger.info("Application started");

            // Logging with fields
            logger.info("User logged in", Map.of(
                    "user_id", "12345",
                    "username", "john_doe",
                    "ip", "192.168.1.100"));

            // Different log levels
            logger.debug("Debug information");
            logger.warning("This is a warning");
            logger.error("An error occurred", Map.of(
                    "error_code", 500,
                    "error_message", "Database connection failed"));

            // Context management
            LogBullLogger sessionLogger = logger.withContext(Map.of(
                    "session_id", "sess_abc123",
                    "user_id", "user_456"));

            sessionLogger.info("Processing user request", Map.of(
                    "action", "purchase",
                    "amount", 99.99));

            // Ensure all logs are sent
            logger.flush();

            // Small delay to allow async sending to complete
            Thread.sleep(2000);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // Clean shutdown
            logger.shutdown();
        }
    }
}
```

#### Context Management

```java
import com.logbull.LogBullLogger;

import java.util.Map;

public class ContextExample {
    public static void main(String[] args) {
        LogBullLogger logger = LogBullLogger.builder()
            .projectId("YOUR_PROJECT_ID")
            .host("http://YOUR_LOGBULL_SERVER:4005")
            .build();

        try {
            // Attach persistent context to all subsequent logs
            LogBullLogger sessionLogger = logger.withContext(Map.of(
                "session_id", "sess_abc123",
                "user_id", "user_456",
                "request_id", "req_789"
            ));

            // All logs from sessionLogger include the context automatically
            sessionLogger.info("User started checkout process", Map.of(
                "cart_items", 3,
                "total_amount", 149.99
            ));
            // Output includes: session_id, user_id, request_id + cart_items, total_amount

            sessionLogger.error("Payment processing failed", Map.of(
                "payment_method", "credit_card",
                "error_code", "DECLINED"
            ));

            // Context can be chained
            LogBullLogger transactionLogger = sessionLogger.withContext(Map.of(
                "transaction_id", "txn_xyz789",
                "merchant_id", "merchant_123"
            ));

            transactionLogger.info("Transaction completed", Map.of(
                "amount", 149.99,
                "currency", "USD"
            ));
            // Includes all previous context + new transaction context

        } finally {
            logger.shutdown();
        }
    }
}
```

### 2. Spring Boot Integration

The easiest way to use LogBull with Spring Boot is via the auto-configuration starter:

#### Maven

```xml
<dependency>
    <groupId>com.logbull</groupId>
    <artifactId>logbull</artifactId>
    <version>RELEASE</version>
</dependency>

<dependency>
    <groupId>com.logbull</groupId>
    <artifactId>logbull-spring-boot-starter</artifactId>
    <version>RELEASE</version>
</dependency>
```

#### Gradle

```groovy
implementation 'com.logbull:logbull:+'
implementation 'com.logbull:logbull-spring-boot-starter:+'
```

#### Configuration - application.yml

```yaml
logbull:
  enabled: true
  project-id: 12345678-1234-1234-1234-123456789012 # required
  host: http://localhost:4005 # required
  api-key: your-api-key
  log-level: INFO
```

#### Configuration - application.properties

```properties
logbull.enabled=true
logbull.project-id=12345678-1234-1234-1234-123456789012
logbull.host=http://localhost:4005
logbull.api-key=your-api-key
logbull.log-level=INFO
```

#### Usage in Spring Service

Once configured, all logs from your Spring Boot application will automatically be sent to LogBull:

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    public void processPayment(String orderId, double amount) {
        // Logs automatically sent to LogBull
        logger.info("Processing payment for order: {}, amount: {}", orderId, amount);
    }
}
```

#### Using MDC for Context

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public void processRequest(String userId, String requestId) {
        // Set MDC context
        MDC.put("user_id", userId);
        MDC.put("request_id", requestId);

        try {
            logger.info("Processing user request");
            // All logs include user_id and request_id

        } finally {
            MDC.clear();
        }
    }
}
```

### 3. SLF4J + Logback Integration

#### Configuration - logback.xml

Place this file in your classpath (e.g., `src/main/resources/logback.xml`):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- Console appender for local development -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- LogBull appender for sending logs to LogBull server -->
    <appender name="LOGBULL" class="com.logbull.slf4j.LogBullLogbackAppender">
        <projectId>12345678-1234-1234-1234-123456789012</projectId>
        <host>http://localhost:4005</host>
        <apiKey>your-api-key</apiKey>
        <logLevel>INFO</logLevel>
    </appender>

    <!-- Root logger configuration -->
    <root level="INFO">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="LOGBULL" />
    </root>
</configuration>
```

#### Usage in Code

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Example demonstrating LogBull with SLF4J/Logback integration.
 *
 * Requires logback.xml configuration file with LogBullLogbackAppender.
 */
public class LogbackExample {
    private static final Logger logger = LoggerFactory.getLogger(LogbackExample.class);

    public static void main(String[] args) {
        try {
            // Basic logging
            logger.info("Application started");

            // Logging with parameters
            String username = "john_doe";
            logger.info("User logged in: {}", username);

            // Using MDC for context
            MDC.put("session_id", "sess_abc123");
            MDC.put("user_id", "user_456");

            try {
                logger.info("Processing user request");
                processPayment("order_123", 99.99);
            } finally {
                MDC.clear();
            }

            // Error logging
            try {
                throw new RuntimeException("Simulated error");
            } catch (Exception e) {
                logger.error("An error occurred", e);
            }

            // Small delay to allow async sending to complete
            Thread.sleep(2000);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void processPayment(String orderId, double amount) {
        MDC.put("order_id", orderId);
        MDC.put("amount", String.valueOf(amount));

        logger.info("Processing payment");
        logger.debug("Payment details loaded");

        MDC.remove("order_id");
        MDC.remove("amount");
    }
}
```

For non-Spring Boot projects or when you need more control, you can configure Logback directly.

## Configuration Options

### Config Parameters

- `projectId` (required): Your LogBull project ID (UUID format)
- `host` (required): LogBull server URL (e.g., `http://localhost:4005`)
- `apiKey` (optional): API key for authentication
- `logLevel` (optional): Minimum log level to process (default: `INFO`)

### Available Log Levels

- `DEBUG`: Detailed information for debugging
- `INFO`: General information messages
- `WARNING`: Warning messages
- `ERROR`: Error messages
- `CRITICAL`: Critical error messages

## API Reference

### LogBullLogger Methods

- `debug(String message)`: Log debug message
- `debug(String message, Map<String, Object> fields)`: Log debug message with fields
- `info(String message)`: Log info message
- `info(String message, Map<String, Object> fields)`: Log info message with fields
- `warning(String message)`: Log warning message
- `warning(String message, Map<String, Object> fields)`: Log warning message with fields
- `error(String message)`: Log error message
- `error(String message, Map<String, Object> fields)`: Log error message with fields
- `critical(String message)`: Log critical message
- `critical(String message, Map<String, Object> fields)`: Log critical message with fields
- `withContext(Map<String, Object> context)`: Create new logger with additional context
- `flush()`: Immediately send all queued logs
- `shutdown()`: Stop background processing and send remaining logs

### Builder Pattern

```java
LogBullLogger logger = LogBullLogger.builder()
    .projectId("12345678-1234-1234-1234-123456789012")
    .host("http://localhost:4005")
    .apiKey("your-api-key")
    .logLevel(LogLevel.INFO)
    .build();
```

## Requirements

- Java 17 or higher
- Optional: SLF4J 2.0+ and Logback 1.4+ for Logback integration

## License

Apache 2.0 License

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## LogBull Server

This library requires a LogBull server instance. Visit [LogBull on GitHub](https://github.com/logbull/logbull) for server setup instructions.

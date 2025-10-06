package com.logbull.spring.autoconfigure;

import ch.qos.logback.classic.LoggerContext;
import com.logbull.Config;
import com.logbull.LogBullLogger;
import com.logbull.slf4j.LogBullLogbackAppender;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Auto-configuration for LogBull integration with Spring Boot.
 */
@AutoConfiguration
@ConditionalOnClass(LogBullLogger.class)
@ConditionalOnProperty(prefix = "logbull", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(LogBullProperties.class)
public class LogBullAutoConfiguration {

    private final LogBullProperties properties;
    private LogBullLogbackAppender appender;

    public LogBullAutoConfiguration(LogBullProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void validateProperties() {
        if (!StringUtils.hasText(properties.getProjectId())) {
            throw new IllegalStateException(
                    "LogBull project-id is required. Please set 'logbull.project-id' in your application properties " +
                            "or disable LogBull by setting 'logbull.enabled=false'");
        }
        if (!StringUtils.hasText(properties.getHost())) {
            throw new IllegalStateException(
                    "LogBull host is required. Please set 'logbull.host' in your application properties " +
                            "or disable LogBull by setting 'logbull.enabled=false'");
        }
    }

    /**
     * Creates a LogBull configuration bean from Spring Boot properties.
     */
    @Bean
    @ConditionalOnMissingBean
    public Config logBullConfig() {
        return Config.builder()
                .projectId(properties.getProjectId())
                .host(properties.getHost())
                .apiKey(properties.getApiKey())
                .logLevel(properties.getLogLevel())
                .build();
    }

    /**
     * Creates a standalone LogBullLogger bean.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "logbull", name = "use-standalone-logger", havingValue = "true")
    public LogBullLogger logBullLogger(Config config) {
        return LogBullLogger.create(config);
    }

    /**
     * Configures Logback appender for automatic log collection.
     */
    @Bean
    @ConditionalOnClass(name = "ch.qos.logback.classic.Logger")
    @ConditionalOnProperty(prefix = "logbull", name = "use-standalone-logger", havingValue = "false", matchIfMissing = true)
    public LogBullLogbackAppender logBullLogbackAppender() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        appender = new LogBullLogbackAppender();
        appender.setContext(context);
        appender.setName("LOGBULL");
        appender.setProjectId(properties.getProjectId());
        appender.setHost(properties.getHost());
        appender.setApiKey(properties.getApiKey());
        appender.setLogLevel(properties.getLogLevel().toString());

        appender.start();

        // Attach to root logger
        ch.qos.logback.classic.Logger rootLogger = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(appender);

        return appender;
    }

    @PreDestroy
    public void cleanup() {
        if (appender != null) {
            appender.stop();
        }
    }
}

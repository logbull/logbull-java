package com.logbull.spring.autoconfigure;

import com.logbull.core.LogLevel;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

/**
 * Configuration properties for LogBull Spring Boot integration.
 */
@ConfigurationProperties(prefix = "logbull")
@Validated
public class LogBullProperties {

    /**
     * Enable LogBull integration.
     */
    private boolean enabled = true;

    /**
     * LogBull project ID (UUID format).
     */
    @NotBlank(message = "LogBull project-id is required. Please set 'logbull.project-id' in your application properties.")
    private String projectId;

    /**
     * LogBull server host URL.
     */
    @NotBlank(message = "LogBull host is required. Please set 'logbull.host' in your application properties.")
    private String host;

    /**
     * API key for authentication (optional).
     */
    private String apiKey;

    /**
     * Minimum log level to process.
     */
    private LogLevel logLevel = LogLevel.INFO;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }
}

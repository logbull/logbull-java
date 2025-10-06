package com.logbull;

import com.logbull.core.LogLevel;
import java.util.Objects;

/**
 * Configuration for LogBull client.
 */
public class Config {
    private final String projectId;
    private final String host;
    private final String apiKey;
    private final LogLevel logLevel;

    private Config(Builder builder) {
        this.projectId = Objects.requireNonNull(builder.projectId, "projectId cannot be null");
        this.host = Objects.requireNonNull(builder.host, "host cannot be null");
        this.apiKey = builder.apiKey;
        this.logLevel = builder.logLevel != null ? builder.logLevel : LogLevel.INFO;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getHost() {
        return host;
    }

    public String getApiKey() {
        return apiKey;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String projectId;
        private String host;
        private String apiKey;
        private LogLevel logLevel;

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

        public Config build() {
            return new Config(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Config config = (Config) o;
        return Objects.equals(projectId, config.projectId) &&
                Objects.equals(host, config.host) &&
                Objects.equals(apiKey, config.apiKey) &&
                logLevel == config.logLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, host, apiKey, logLevel);
    }

    @Override
    public String toString() {
        return "Config{" +
                "projectId='" + projectId + '\'' +
                ", host='" + host + '\'' +
                ", logLevel=" + logLevel +
                '}';
    }
}

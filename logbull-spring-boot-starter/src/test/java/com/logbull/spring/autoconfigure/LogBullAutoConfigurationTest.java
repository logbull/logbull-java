package com.logbull.spring.autoconfigure;

import com.logbull.Config;
import com.logbull.LogBullLogger;
import com.logbull.core.LogLevel;
import com.logbull.slf4j.LogBullLogbackAppender;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class LogBullAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LogBullAutoConfiguration.class));

    @Test
    void autoConfigurationIsDisabledWhenEnabledIsFalse() {
        contextRunner
                .withPropertyValues(
                        "logbull.enabled=false",
                        "logbull.project-id=12345678-1234-1234-1234-123456789012",
                        "logbull.host=http://localhost:4005")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(Config.class);
                    assertThat(context).doesNotHaveBean(LogBullLogbackAppender.class);
                });
    }

    @Test
    void configBeanIsCreatedWithProperties() {
        contextRunner
                .withPropertyValues(
                        "logbull.enabled=true",
                        "logbull.project-id=12345678-1234-1234-1234-123456789012",
                        "logbull.host=http://localhost:4005",
                        "logbull.api-key=test-api-key",
                        "logbull.log-level=DEBUG")
                .run(context -> {
                    assertThat(context).hasSingleBean(Config.class);
                    Config config = context.getBean(Config.class);
                    assertThat(config.getProjectId()).isEqualTo("12345678-1234-1234-1234-123456789012");
                    assertThat(config.getHost()).isEqualTo("http://localhost:4005");
                    assertThat(config.getApiKey()).isEqualTo("test-api-key");
                    assertThat(config.getLogLevel()).isEqualTo(LogLevel.DEBUG);
                });
    }

    @Test
    void logbackAppenderIsCreatedByDefault() {
        contextRunner
                .withPropertyValues(
                        "logbull.project-id=12345678-1234-1234-1234-123456789012",
                        "logbull.host=http://localhost:4005")
                .run(context -> {
                    assertThat(context).hasSingleBean(LogBullLogbackAppender.class);
                    assertThat(context).doesNotHaveBean(LogBullLogger.class);
                });
    }

    @Test
    void standaloneLoggerIsCreatedWhenConfigured() {
        contextRunner
                .withPropertyValues(
                        "logbull.project-id=12345678-1234-1234-1234-123456789012",
                        "logbull.host=http://localhost:4005",
                        "logbull.use-standalone-logger=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(LogBullLogger.class);
                });
    }

    @Test
    void defaultLogLevelIsInfo() {
        contextRunner
                .withPropertyValues(
                        "logbull.project-id=12345678-1234-1234-1234-123456789012",
                        "logbull.host=http://localhost:4005")
                .run(context -> {
                    Config config = context.getBean(Config.class);
                    assertThat(config.getLogLevel()).isEqualTo(LogLevel.INFO);
                });
    }
}

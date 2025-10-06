package com.logbull.spring.autoconfigure;

import com.logbull.core.LogLevel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LogBullPropertiesTest {

    @Test
    void defaultValues() {
        LogBullProperties properties = new LogBullProperties();

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getLogLevel()).isEqualTo(LogLevel.INFO);
        assertThat(properties.getProjectId()).isNull();
        assertThat(properties.getHost()).isNull();
        assertThat(properties.getApiKey()).isNull();
    }

    @Test
    void settersAndGetters() {
        LogBullProperties properties = new LogBullProperties();

        properties.setEnabled(false);
        properties.setProjectId("12345678-1234-1234-1234-123456789012");
        properties.setHost("http://localhost:4005");
        properties.setApiKey("test-key");
        properties.setLogLevel(LogLevel.DEBUG);

        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.getProjectId()).isEqualTo("12345678-1234-1234-1234-123456789012");
        assertThat(properties.getHost()).isEqualTo("http://localhost:4005");
        assertThat(properties.getApiKey()).isEqualTo("test-key");
        assertThat(properties.getLogLevel()).isEqualTo(LogLevel.DEBUG);
    }
}

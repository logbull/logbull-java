package com.logbull.internal.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = new Validator();
    }

    @Test
    void testValidProjectId() {
        assertDoesNotThrow(() -> validator.validateProjectId("12345678-1234-1234-1234-123456789012"));
    }

    @Test
    void testInvalidProjectIdFormat() {
        assertThrows(IllegalArgumentException.class, () -> validator.validateProjectId("invalid"));
    }

    @Test
    void testEmptyProjectId() {
        assertThrows(IllegalArgumentException.class, () -> validator.validateProjectId(""));
    }

    @Test
    void testValidHttpUrl() {
        assertDoesNotThrow(() -> validator.validateHostUrl("http://localhost:4005"));
    }

    @Test
    void testValidHttpsUrl() {
        assertDoesNotThrow(() -> validator.validateHostUrl("https://logbull.example.com"));
    }

    @Test
    void testInvalidUrlScheme() {
        assertThrows(IllegalArgumentException.class, () -> validator.validateHostUrl("ftp://example.com"));
    }

    @Test
    void testEmptyUrl() {
        assertThrows(IllegalArgumentException.class, () -> validator.validateHostUrl(""));
    }

    @Test
    void testValidApiKey() {
        assertDoesNotThrow(() -> validator.validateApiKey("abc123_xyz-789.test"));
    }

    @Test
    void testShortApiKey() {
        assertThrows(IllegalArgumentException.class, () -> validator.validateApiKey("short"));
    }

    @Test
    void testInvalidApiKeyCharacters() {
        assertThrows(IllegalArgumentException.class, () -> validator.validateApiKey("invalid@key!"));
    }

    @Test
    void testNullApiKey() {
        assertDoesNotThrow(() -> validator.validateApiKey(null));
    }

    @Test
    void testValidLogMessage() {
        assertDoesNotThrow(() -> validator.validateLogMessage("This is a valid log message"));
    }

    @Test
    void testEmptyLogMessage() {
        assertThrows(IllegalArgumentException.class, () -> validator.validateLogMessage(""));
    }

    @Test
    void testTooLongLogMessage() {
        String longMessage = "a".repeat(10_001);
        assertThrows(IllegalArgumentException.class, () -> validator.validateLogMessage(longMessage));
    }

    @Test
    void testValidFields() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("user_id", "12345");
        fields.put("action", "login");

        assertDoesNotThrow(() -> validator.validateLogFields(fields));
    }

    @Test
    void testTooManyFields() {
        Map<String, Object> fields = new HashMap<>();
        for (int i = 0; i < 101; i++) {
            fields.put("field_" + i, i);
        }

        assertThrows(IllegalArgumentException.class, () -> validator.validateLogFields(fields));
    }

    @Test
    void testEmptyFieldKey() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("", "value");

        assertThrows(IllegalArgumentException.class, () -> validator.validateLogFields(fields));
    }

    @Test
    void testTooLongFieldKey() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("a".repeat(101), "value");

        assertThrows(IllegalArgumentException.class, () -> validator.validateLogFields(fields));
    }
}

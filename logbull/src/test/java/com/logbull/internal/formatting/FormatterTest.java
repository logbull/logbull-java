package com.logbull.internal.formatting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FormatterTest {
    private Formatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new Formatter();
    }

    @Test
    void testFormatMessage() {
        String message = "  Test message  ";
        String formatted = formatter.formatMessage(message);
        assertEquals("Test message", formatted);
    }

    @Test
    void testFormatLongMessage() {
        String longMessage = "a".repeat(15_000);
        String formatted = formatter.formatMessage(longMessage);
        assertTrue(formatted.length() <= 10_000);
        assertTrue(formatted.endsWith("..."));
    }

    @Test
    void testFormatNullMessage() {
        String formatted = formatter.formatMessage(null);
        assertEquals("", formatted);
    }

    @Test
    void testEnsureFields() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("user_id", "12345");
        fields.put("count", 42);

        Map<String, Object> ensured = formatter.ensureFields(fields);
        assertEquals(2, ensured.size());
        assertEquals("12345", ensured.get("user_id"));
        assertEquals(42, ensured.get("count"));
    }

    @Test
    void testEnsureNullFields() {
        Map<String, Object> ensured = formatter.ensureFields(null);
        assertNotNull(ensured);
        assertTrue(ensured.isEmpty());
    }

    @Test
    void testEnsureFieldsWithEmptyKeys() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("", "should be skipped");
        fields.put("  ", "should be skipped");
        fields.put("valid", "should be kept");

        Map<String, Object> ensured = formatter.ensureFields(fields);
        assertEquals(1, ensured.size());
        assertTrue(ensured.containsKey("valid"));
    }

    @Test
    void testMergeFields() {
        Map<String, Object> base = Map.of("a", 1, "b", 2);
        Map<String, Object> additional = Map.of("c", 3, "b", 20);

        Map<String, Object> merged = formatter.mergeFields(base, additional);
        assertEquals(3, merged.size());
        assertEquals(1, merged.get("a"));
        assertEquals(20, merged.get("b")); // Should be overridden
        assertEquals(3, merged.get("c"));
    }

    @Test
    void testMergeWithNullBase() {
        Map<String, Object> additional = Map.of("a", 1);
        Map<String, Object> merged = formatter.mergeFields(null, additional);
        assertEquals(1, merged.size());
        assertEquals(1, merged.get("a"));
    }

    @Test
    void testMergeWithNullAdditional() {
        Map<String, Object> base = Map.of("a", 1);
        Map<String, Object> merged = formatter.mergeFields(base, null);
        assertEquals(1, merged.size());
        assertEquals(1, merged.get("a"));
    }
}

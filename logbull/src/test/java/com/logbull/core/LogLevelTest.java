package com.logbull.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LogLevelTest {

    @Test
    void testLogLevelPriorities() {
        assertTrue(LogLevel.DEBUG.getPriority() < LogLevel.INFO.getPriority());
        assertTrue(LogLevel.INFO.getPriority() < LogLevel.WARNING.getPriority());
        assertTrue(LogLevel.WARNING.getPriority() < LogLevel.ERROR.getPriority());
        assertTrue(LogLevel.ERROR.getPriority() < LogLevel.CRITICAL.getPriority());
    }

    @Test
    void testLogLevelToString() {
        assertEquals("DEBUG", LogLevel.DEBUG.toString());
        assertEquals("INFO", LogLevel.INFO.toString());
        assertEquals("WARNING", LogLevel.WARNING.toString());
        assertEquals("ERROR", LogLevel.ERROR.toString());
        assertEquals("CRITICAL", LogLevel.CRITICAL.toString());
    }

    @Test
    void testLogLevelValues() {
        assertEquals(10, LogLevel.DEBUG.getPriority());
        assertEquals(20, LogLevel.INFO.getPriority());
        assertEquals(30, LogLevel.WARNING.getPriority());
        assertEquals(40, LogLevel.ERROR.getPriority());
        assertEquals(50, LogLevel.CRITICAL.getPriority());
    }
}

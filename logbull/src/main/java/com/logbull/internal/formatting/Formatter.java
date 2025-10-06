package com.logbull.internal.formatting;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Formats log messages and fields for sending to LogBull.
 */
public class Formatter {
    private static final int MAX_MESSAGE_LENGTH = 10_000;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public String formatMessage(String message) {
        if (message == null) {
            return "";
        }

        String trimmed = message.trim();
        if (trimmed.length() > MAX_MESSAGE_LENGTH) {
            return trimmed.substring(0, MAX_MESSAGE_LENGTH - 3) + "...";
        }
        return trimmed;
    }

    public Map<String, Object> ensureFields(Map<String, Object> fields) {
        if (fields == null) {
            return new HashMap<>();
        }

        Map<String, Object> formatted = new HashMap<>();
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            String key = entry.getKey();
            if (key == null) {
                continue;
            }

            key = key.trim();
            if (key.isEmpty()) {
                continue;
            }

            Object value = entry.getValue();
            if (isJsonSerializable(value)) {
                formatted.put(key, value);
            } else {
                formatted.put(key, convertToString(value));
            }
        }

        return formatted;
    }

    public Map<String, Object> mergeFields(Map<String, Object> base, Map<String, Object> additional) {
        Map<String, Object> result = new HashMap<>(ensureFields(base));
        result.putAll(ensureFields(additional));
        return result;
    }

    private boolean isJsonSerializable(Object value) {
        if (value == null) {
            return true;
        }

        try {
            OBJECT_MAPPER.writeValueAsString(value);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    private String convertToString(Object value) {
        if (value == null) {
            return "null";
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }
}

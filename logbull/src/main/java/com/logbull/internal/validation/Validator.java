package com.logbull.internal.validation;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Validates configuration and log entry data.
 */
public class Validator {
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    private static final Pattern API_KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-.]{10,}$");
    private static final int MAX_MESSAGE_LENGTH = 10_000;
    private static final int MAX_FIELDS_COUNT = 100;
    private static final int MAX_FIELD_KEY_LENGTH = 100;

    public void validateProjectId(String projectId) {
        if (projectId == null || projectId.trim().isEmpty()) {
            throw new IllegalArgumentException("project ID cannot be empty");
        }

        String trimmed = projectId.trim();
        if (!UUID_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                    String.format(
                            "invalid project ID format '%s'. Must be a valid UUID format: " +
                                    "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
                            trimmed));
        }
    }

    public void validateHostUrl(String host) {
        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("host URL cannot be empty");
        }

        String trimmed = host.trim();
        try {
            URL url = new URI(trimmed).toURL();
            String scheme = url.getProtocol();
            if (!"http".equals(scheme) && !"https".equals(scheme)) {
                throw new IllegalArgumentException(
                        String.format("host URL must use http or https scheme, got: %s", scheme));
            }
            if (url.getHost() == null || url.getHost().isEmpty()) {
                throw new IllegalArgumentException("host URL must have a host component");
            }
        } catch (URISyntaxException | MalformedURLException e) {
            throw new IllegalArgumentException("invalid host URL format: " + e.getMessage(), e);
        }
    }

    public void validateApiKey(String apiKey) {
        if (apiKey == null) {
            return;
        }

        String trimmed = apiKey.trim();
        if (trimmed.isEmpty()) {
            return;
        }

        if (trimmed.length() < 10) {
            throw new IllegalArgumentException("API key must be at least 10 characters long");
        }

        if (!API_KEY_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                    "invalid API key format. API key must contain only alphanumeric characters, " +
                            "underscores, hyphens, and dots");
        }
    }

    public void validateLogMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("log message cannot be empty");
        }

        if (message.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException(
                    String.format(
                            "log message too long (%d chars). Maximum allowed: %d",
                            message.length(),
                            MAX_MESSAGE_LENGTH));
        }
    }

    public void validateLogFields(Map<String, Object> fields) {
        if (fields == null) {
            return;
        }

        if (fields.size() > MAX_FIELDS_COUNT) {
            throw new IllegalArgumentException(
                    String.format(
                            "too many fields (%d). Maximum allowed: %d",
                            fields.size(),
                            MAX_FIELDS_COUNT));
        }

        for (String key : fields.keySet()) {
            if (key == null || key.trim().isEmpty()) {
                throw new IllegalArgumentException("field key cannot be empty");
            }

            if (key.length() > MAX_FIELD_KEY_LENGTH) {
                throw new IllegalArgumentException(
                        String.format(
                                "field key too long (%d chars). Maximum: %d",
                                key.length(),
                                MAX_FIELD_KEY_LENGTH));
            }
        }
    }
}

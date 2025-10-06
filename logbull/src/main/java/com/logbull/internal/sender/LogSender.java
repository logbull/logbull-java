package com.logbull.internal.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logbull.Config;
import com.logbull.core.LogBatch;
import com.logbull.core.LogBullResponse;
import com.logbull.core.LogEntry;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handles asynchronous sending of log batches to LogBull server.
 */
public class LogSender {
    private static final int BATCH_SIZE = 1_000;
    private static final int BATCH_INTERVAL_MS = 1_000;
    private static final int QUEUE_CAPACITY = 10_000;
    private static final int MIN_WORKERS = 1;
    private static final int MAX_WORKERS = 10;
    private static final int HTTP_TIMEOUT_MS = 30_000;

    private final Config config;
    private final ObjectMapper objectMapper;
    private final BlockingQueue<LogEntry> logQueue;
    private final ExecutorService batchProcessor;
    private final ExecutorService httpExecutor;
    private final Semaphore workerSemaphore;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean shutdown;

    public LogSender(Config config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();
        this.logQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
        this.batchProcessor = Executors.newSingleThreadExecutor(
                r -> new Thread(r, "LogBull-BatchProcessor"));
        this.httpExecutor = Executors.newFixedThreadPool(
                MAX_WORKERS,
                r -> new Thread(r, "LogBull-HttpSender"));
        this.workerSemaphore = new Semaphore(MIN_WORKERS);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(
                r -> new Thread(r, "LogBull-Scheduler"));
        this.shutdown = new AtomicBoolean(false);

        startBatchProcessor();
    }

    public void addLog(LogEntry entry) {
        if (shutdown.get()) {
            return;
        }

        boolean added = logQueue.offer(entry);
        if (!added) {
            System.err.println("LogBull: log queue full, dropping log");
        }
    }

    public void flush() {
        sendBatch();
    }

    public void shutdown() {
        if (shutdown.getAndSet(true)) {
            return;
        }

        sendBatch();

        scheduler.shutdown();
        batchProcessor.shutdown();

        try {
            if (!batchProcessor.awaitTermination(5, TimeUnit.SECONDS)) {
                batchProcessor.shutdownNow();
            }
        } catch (InterruptedException e) {
            batchProcessor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        httpExecutor.shutdown();
        try {
            if (!httpExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                httpExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            httpExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void startBatchProcessor() {
        scheduler.scheduleAtFixedRate(
                this::sendBatch,
                BATCH_INTERVAL_MS,
                BATCH_INTERVAL_MS,
                TimeUnit.MILLISECONDS);
    }

    private void sendBatch() {
        if (shutdown.get() && logQueue.isEmpty()) {
            return;
        }

        List<LogEntry> logs = new ArrayList<>();
        logQueue.drainTo(logs, BATCH_SIZE);

        if (logs.isEmpty()) {
            return;
        }

        if (workerSemaphore.tryAcquire()) {
            httpExecutor.submit(() -> {
                try {
                    sendHttpRequest(logs);
                } finally {
                    workerSemaphore.release();
                }
            });
        } else {
            httpExecutor.submit(() -> sendHttpRequest(logs));
        }
    }

    private void sendHttpRequest(List<LogEntry> logs) {
        try {
            LogBatch batch = new LogBatch(logs);
            String json = objectMapper.writeValueAsString(createBatchPayload(batch));

            String urlString = String.format(
                    "%s/api/v1/logs/receiving/%s",
                    config.getHost(),
                    config.getProjectId());

            URL url = new URI(urlString).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "LogBull-Java-Client/1.0");

            if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
                connection.setRequestProperty("X-API-Key", config.getApiKey());
            }

            connection.setDoOutput(true);
            connection.setConnectTimeout(HTTP_TIMEOUT_MS);
            connection.setReadTimeout(HTTP_TIMEOUT_MS);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == 200 || responseCode == 202) {
                try {
                    String responseBody = new String(
                            connection.getInputStream().readAllBytes(),
                            StandardCharsets.UTF_8);
                    LogBullResponse response = parseResponse(responseBody);
                    if (response.getRejected() > 0) {
                        handleRejectedLogs(response, logs);
                    }
                } catch (Exception e) {
                    // Response parsing failed, but logs were accepted
                }
            } else {
                String errorBody = new String(
                        connection.getErrorStream().readAllBytes(),
                        StandardCharsets.UTF_8);
                System.err.println(
                        "LogBull: server returned status " + responseCode + ": " + errorBody);
            }

        } catch (IOException e) {
            System.err.println("LogBull: HTTP request failed: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("LogBull: failed to send batch: " + e.getMessage());
        }
    }

    private Map<String, Object> createBatchPayload(LogBatch batch) {
        List<Map<String, Object>> logsList = new ArrayList<>();
        for (LogEntry entry : batch.getLogs()) {
            Map<String, Object> logMap = Map.of(
                    "level", entry.getLevel(),
                    "message", entry.getMessage(),
                    "timestamp", entry.getTimestamp(),
                    "fields", entry.getFields());
            logsList.add(logMap);
        }
        return Map.of("logs", logsList);
    }

    private LogBullResponse parseResponse(String json) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.readValue(json, Map.class);

            int accepted = ((Number) map.getOrDefault("accepted", 0)).intValue();
            int rejected = ((Number) map.getOrDefault("rejected", 0)).intValue();
            String message = (String) map.get("message");

            List<LogBullResponse.RejectedLog> errors = new ArrayList<>();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> errorsList = (List<Map<String, Object>>) map.get("errors");

            if (errorsList != null) {
                for (Map<String, Object> error : errorsList) {
                    int index = ((Number) error.get("index")).intValue();
                    String errorMessage = (String) error.get("message");
                    errors.add(new LogBullResponse.RejectedLog(index, errorMessage));
                }
            }

            return new LogBullResponse(accepted, rejected, message, errors);
        } catch (Exception e) {
            return new LogBullResponse(0, 0, "Failed to parse response", null);
        }
    }

    private void handleRejectedLogs(LogBullResponse response, List<LogEntry> sentLogs) {
        System.err.println("LogBull: Rejected " + response.getRejected() + " log entries");

        if (!response.getErrors().isEmpty()) {
            System.err.println("LogBull: Rejected log details:");
            for (LogBullResponse.RejectedLog error : response.getErrors()) {
                int index = error.getIndex();
                if (index >= 0 && index < sentLogs.size()) {
                    LogEntry log = sentLogs.get(index);
                    System.err.println("  - Log #" + index + " rejected (" + error.getMessage() + "):");
                    System.err.println("    Level: " + log.getLevel());
                    System.err.println("    Message: " + log.getMessage());
                    System.err.println("    Timestamp: " + log.getTimestamp());
                    if (!log.getFields().isEmpty()) {
                        System.err.println("    Fields: " + log.getFields());
                    }
                }
            }
        }
    }
}

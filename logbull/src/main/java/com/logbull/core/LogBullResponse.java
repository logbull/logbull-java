package com.logbull.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Response from LogBull server after sending logs.
 */
public class LogBullResponse {
    private final int accepted;
    private final int rejected;
    private final String message;
    private final List<RejectedLog> errors;

    public LogBullResponse(int accepted, int rejected, String message, List<RejectedLog> errors) {
        this.accepted = accepted;
        this.rejected = rejected;
        this.message = message;
        this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
    }

    public int getAccepted() {
        return accepted;
    }

    public int getRejected() {
        return rejected;
    }

    public String getMessage() {
        return message;
    }

    public List<RejectedLog> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        LogBullResponse that = (LogBullResponse) o;
        return accepted == that.accepted &&
                rejected == that.rejected &&
                Objects.equals(message, that.message) &&
                Objects.equals(errors, that.errors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accepted, rejected, message, errors);
    }

    @Override
    public String toString() {
        return "LogBullResponse{" +
                "accepted=" + accepted +
                ", rejected=" + rejected +
                ", message='" + message + '\'' +
                ", errors=" + errors +
                '}';
    }

    public static class RejectedLog {
        private final int index;
        private final String message;

        public RejectedLog(int index, String message) {
            this.index = index;
            this.message = message;
        }

        public int getIndex() {
            return index;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            RejectedLog that = (RejectedLog) o;
            return index == that.index && Objects.equals(message, that.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(index, message);
        }

        @Override
        public String toString() {
            return "RejectedLog{" +
                    "index=" + index +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}

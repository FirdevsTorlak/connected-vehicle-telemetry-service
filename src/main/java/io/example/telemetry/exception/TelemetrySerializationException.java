package io.example.telemetry.exception;

public class TelemetrySerializationException extends RuntimeException {
    public TelemetrySerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}

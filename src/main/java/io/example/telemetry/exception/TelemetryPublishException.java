package io.example.telemetry.exception;

public class TelemetryPublishException extends RuntimeException {
    public TelemetryPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}

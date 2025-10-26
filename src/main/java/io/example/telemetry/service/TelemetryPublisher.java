package io.example.telemetry.service;

import io.example.telemetry.model.TelemetryMessage;

public interface TelemetryPublisher {
    void publish(TelemetryMessage msg);
}

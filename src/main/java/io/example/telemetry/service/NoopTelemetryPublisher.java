package io.example.telemetry.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.example.telemetry.model.TelemetryMessage;

/**
 * No-op TelemetryPublisher used when Kafka is disabled.
 * This bean is always registered. When Kafka is enabled, the
 * KafkaTelemetryPublisher is marked @Primary, so it will be injected instead.
 */
@Service
public class NoopTelemetryPublisher implements TelemetryPublisher {

    private static final Logger log = LoggerFactory.getLogger(NoopTelemetryPublisher.class);

    @Override
    public void publish(TelemetryMessage msg) {
        // Keep local development fast and deterministic when Kafka is off.
        if (log.isDebugEnabled()) {
            log.debug("Noop publish (Kafka disabled): vehicleId={}, speedKph={}",
                    msg.getVehicleId(), msg.getSpeedKph());
        }
    }
}
package io.example.telemetry.service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.example.telemetry.exception.TelemetryPublishException;
import io.example.telemetry.exception.TelemetrySerializationException;
import io.example.telemetry.model.TelemetryMessage;

/**
 * Kafka-backed implementation of TelemetryPublisher.
 * Becomes active (and primary) only when `app.kafka.enabled=true`.
 * When disabled, NoopTelemetryPublisher will be used instead.
 */
@Service
@Primary
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class KafkaTelemetryPublisher implements TelemetryPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaTelemetryPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public KafkaTelemetryPublisher(KafkaTemplate<String, String> kafkaTemplate,
                                   ObjectMapper objectMapper,
                                   @Value("${app.kafka.topic:telemetry-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    @Override
    public void publish(TelemetryMessage msg) {
        final String key = msg.getVehicleId();
        final String json;
        try {
            json = objectMapper.writeValueAsString(msg);
        } catch (JsonProcessingException e) {
            throw new TelemetrySerializationException("Failed to serialize telemetry message", e);
        }

        try {
            var future = kafkaTemplate.send(topic, key, json);
            var result = future.get(5, TimeUnit.SECONDS);

            // Guard logging to avoid unnecessary work when DEBUG is off
            if (log.isDebugEnabled()) {
                RecordMetadata meta = result.getRecordMetadata();
                log.debug("Published to topic={} partition={} offset={}, key={}",
                        meta.topic(), meta.partition(), meta.offset(), key);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TelemetryPublishException("Interrupted while publishing to Kafka", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new TelemetryPublishException("Failed to publish telemetry to Kafka", e);
        }
    }
}
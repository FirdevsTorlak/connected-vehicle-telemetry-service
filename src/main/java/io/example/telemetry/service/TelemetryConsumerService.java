package io.example.telemetry.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class TelemetryConsumerService {

    private final Deque<String> ring = new ArrayDeque<>(200);

    @KafkaListener(topics = "${app.kafka.topic:telemetry-events}", groupId = "${spring.kafka.consumer.group-id:telemetry-consumer}")
    public void onMessage(String json) {
        if (ring.size() >= 200) {
            ring.removeFirst();
        }
        ring.addLast(json);
    }

    public List<String> recent() {
        return new ArrayList<>(ring);
    }
}

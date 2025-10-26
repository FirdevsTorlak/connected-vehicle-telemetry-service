package io.example.telemetry.web;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.example.telemetry.model.TelemetryMessage;
import io.example.telemetry.service.TelemetryConsumerService;
import io.example.telemetry.service.TelemetryPublisher;

@RestController
@RequestMapping("/api/telemetry")
public class TelemetryController {

    private final TelemetryPublisher publisher;
    private final TelemetryConsumerService consumer; // null when Kafka disabled
    private final ObjectMapper mapper;

    public TelemetryController(TelemetryPublisher publisher,
                               ObjectProvider<TelemetryConsumerService> consumerProvider,
                               ObjectMapper mapper) {
        this.publisher = publisher;
        this.consumer = consumerProvider.getIfAvailable();
        this.mapper = mapper;
    }

    @PostMapping(path = "/publish", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<Void> publish(@RequestBody(required = false) String body) {
        Map<String, Object> payload = parseBody(body);
        String vehicleId = String.valueOf(payload.getOrDefault("vehicleId", "unknown"));
        double speed = parseDouble(payload.get("speedKph"));
        TelemetryMessage msg = new TelemetryMessage(vehicleId, speed, Instant.now(), payload);
        publisher.publish(msg);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/recent")
    public ResponseEntity<List<String>> recent() {
        if (consumer == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(consumer.recent());
    }

    private Map<String, Object> parseBody(String body) {
        if (body == null || body.isBlank()) return Collections.emptyMap();

        // Try JSON first
        try {
            return mapper.readValue(body, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ignored) {
            // Fallback: form-encoded "vehicleId=WDB&speedKph=80.5"
            try {
                Map<String, String> form = Arrays.stream(body.split("&"))
                        .map(s -> s.split("=", 2))
                        .filter(p -> p.length == 2)
                        .collect(Collectors.toMap(
                                p -> urlDecode(p[0]),
                                p -> urlDecode(p[1])
                        ));

                // Convert Map<String,String> -> Map<String,Object> (tip güvenli)
                Map<String, Object> any = new HashMap<>();
                form.forEach(any::put);
                return any;
            } catch (Exception e) {
                return Collections.emptyMap();
            }
        }
    }

    private static double parseDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number number) return number.doubleValue();
        if (value instanceof String string) {
            try { return Double.parseDouble(string); }
            catch (NumberFormatException ignored) { return 0.0; }
        }
        return 0.0;
    }

    private static String urlDecode(String s) {
        try { return java.net.URLDecoder.decode(s, java.nio.charset.StandardCharsets.UTF_8); }
        catch (Exception e) { return s; }
    }
}
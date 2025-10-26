package io.example.telemetry;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "app.kafka.enabled=false", // No-Kafka mode for fast & stable tests
        "server.port=0"            // random port; MockMvc doesn’t need a real port
})
@AutoConfigureMockMvc
class TelemetryApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void publishEndpoint_returns202_withoutKafka() throws Exception {
        mvc.perform(post("/api/telemetry/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"vehicleId\":\"WDB-TEST-001\",\"speedKph\":80.5}"))
           .andExpect(status().isAccepted());
    }

    @Test
    void recent_returnsEmptyList_whenKafkaDisabled() throws Exception {
        mvc.perform(get("/api/telemetry/recent"))
           .andExpect(status().isOk())
           .andExpect(content().json("[]"));
    }

    @Test
    void health_returnsUp() throws Exception {
        mvc.perform(get("/actuator/health"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.status").value("UP"));
    }
}
package io.example.telemetry.model;

import java.time.Instant;
import java.util.Map;

public class TelemetryMessage {
    private String vehicleId;
    private double speedKph;
    private Instant ts;
    private Map<String, Object> extra;

    public TelemetryMessage() {}

    public TelemetryMessage(String vehicleId, double speedKph, Instant ts, Map<String, Object> extra) {
        this.vehicleId = vehicleId;
        this.speedKph = speedKph;
        this.ts = ts;
        this.extra = extra;
    }

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public double getSpeedKph() { return speedKph; }
    public void setSpeedKph(double speedKph) { this.speedKph = speedKph; }

    public Instant getTs() { return ts; }
    public void setTs(Instant ts) { this.ts = ts; }

    public Map<String, Object> getExtra() { return extra; }
    public void setExtra(Map<String, Object> extra) { this.extra = extra; }
}

package com.campus.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Sensor {
    private String id;
    private String type;
    private String roomId;
    private String status;
    private double currentValue;
    private List<Reading> readings = new ArrayList<>();

    public Sensor() {}

    @JsonProperty("id")
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @JsonProperty("type")
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    @JsonProperty("roomId")
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    @JsonProperty("status")
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @JsonProperty("currentValue")
    public double getCurrentValue() { return currentValue; }
    public void setCurrentValue(double currentValue) { this.currentValue = currentValue; }

    @JsonProperty("readings")
    public List<Reading> getReadings() { return readings; }
    public void setReadings(List<Reading> readings) { this.readings = readings; }

    public void addReading(Reading reading) {
        this.readings.add(reading);
        this.currentValue = reading.getValue();
    }
}

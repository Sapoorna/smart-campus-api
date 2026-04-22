package com.campus.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SensorRoom {
    private String id;
    private String name;
    private int capacity;
    private String building;
    private List<String> sensorIds = new ArrayList<>();

    public SensorRoom() {}

    @JsonProperty("id")
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @JsonProperty("name")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @JsonProperty("capacity")
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    @JsonProperty("building")
    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    @JsonProperty("sensorIds")
    public List<String> getSensorIds() { return sensorIds; }
    public void setSensorIds(List<String> sensorIds) { this.sensorIds = sensorIds; }

    public void addSensorId(String sensorId) { this.sensorIds.add(sensorId); }
    public void removeSensorId(String sensorId) { this.sensorIds.remove(sensorId); }
}

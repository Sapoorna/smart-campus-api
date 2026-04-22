package com.campus.resource;

import com.campus.exception.SensorUnavailableException;
import com.campus.model.Reading;
import com.campus.model.Sensor;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final Map<String, Sensor> sensors = SensorResource.getSensors();
    private static final AtomicInteger readingIdCounter = new AtomicInteger(1);
    private String sensorId;

    public SensorReadingResource() {}

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings() {
        Sensor sensor = sensors.get(sensorId);
        if (sensor == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Sensor not found with id: " + sensorId);
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        return Response.ok(sensor.getReadings()).build();
    }

    @POST
    public Response addReading(Reading reading) {
        Sensor sensor = sensors.get(sensorId);
        if (sensor == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Sensor not found with id: " + sensorId);
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException("Sensor '" + sensorId + "' is in MAINTENANCE mode.");
        }
        String id = String.valueOf(readingIdCounter.getAndIncrement());
        reading.setId(id);
        reading.setSensorId(sensorId);
        sensor.addReading(reading);
        return Response.status(Response.Status.CREATED)
                       .header("Location", "/api/v1/sensors/" + sensorId + "/readings/" + id)
                       .entity(reading)
                       .build();
    }
}


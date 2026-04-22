package com.campus.resource;

import com.campus.exception.LinkedResourceNotFoundException;
import com.campus.model.Sensor;
import com.campus.model.SensorRoom;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    public static final Map<String, Sensor> sensors = new HashMap<>();
    private static final AtomicInteger idCounter = new AtomicInteger(1);

    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensorList = new ArrayList<>(sensors.values());
        if (type != null && !type.isEmpty()) {
            sensorList = sensorList.stream()
                .filter(s -> type.equalsIgnoreCase(s.getType()))
                .collect(Collectors.toList());
        }
        return Response.ok(sensorList).build();
    }

    @POST
    public Response createSensor(Sensor sensor) {
        Map<String, SensorRoom> rooms = RoomResource.getRooms();
        String roomId = sensor.getRoomId();
        if (roomId == null || !rooms.containsKey(roomId)) {
            throw new LinkedResourceNotFoundException("Room with id '" + roomId + "' does not exist.");
        }
        String id = String.valueOf(idCounter.getAndIncrement());
        sensor.setId(id);
        if (sensor.getStatus() == null) sensor.setStatus("ACTIVE");
        sensors.put(id, sensor);
        rooms.get(roomId).addSensorId(id);
        return Response.status(Response.Status.CREATED)
                       .header("Location", "/api/v1/sensors/" + id)
                       .entity(sensor)
                       .build();
    }

    @GET
    @Path("/{id}")
    public Response getSensorById(@PathParam("id") String id) {
        Sensor sensor = sensors.get(id);
        if (sensor == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Sensor not found with id: " + id);
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        return Response.ok(sensor).build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }

    public static Map<String, Sensor> getSensors() {
        return sensors;
    }
}
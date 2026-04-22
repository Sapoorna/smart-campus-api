package com.campus.resource;

import com.campus.exception.RoomNotEmptyException;
import com.campus.model.SensorRoom;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    public static final Map<String, SensorRoom> rooms = new HashMap<>();
    private static final AtomicInteger idCounter = new AtomicInteger(1);

    @GET
    public Response getAllRooms() {
        List<SensorRoom> roomList = new ArrayList<>(rooms.values());
        return Response.ok(roomList).build();
    }

    @POST
    public Response createRoom(SensorRoom room) {
        String id = String.valueOf(idCounter.getAndIncrement());
        room.setId(id);
        rooms.put(id, room);
        return Response.status(Response.Status.CREATED)
                       .header("Location", "/api/v1/rooms/" + id)
                       .entity(room)
                       .build();
    }

    @GET
    @Path("/{id}")
    public Response getRoomById(@PathParam("id") String id) {
        SensorRoom room = rooms.get(id);
        if (room == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Room not found with id: " + id);
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteRoom(@PathParam("id") String id) {
        SensorRoom room = rooms.get(id);
        if (room == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Room not found with id: " + id);
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Cannot delete room '" + id + "'. It still has " + room.getSensorIds().size() + " active sensor(s).");
        }
        rooms.remove(id);
        return Response.noContent().build();
    }

    public static Map<String, SensorRoom> getRooms() {
        return rooms;
    }
}
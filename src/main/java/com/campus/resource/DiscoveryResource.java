package com.campus.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response getApiInfo() {
        Map<String, Object> apiInfo = new HashMap<>();
        apiInfo.put("version", "1.0.0");
        apiInfo.put("contact", "smartcampus@university.edu");
        apiInfo.put("description", "Smart Campus API for Room and Sensor Management");
        Map<String, String> resources = new HashMap<>();
        resources.put("rooms", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");
        apiInfo.put("resources", resources);
        return Response.ok(apiInfo).build();
    }
}
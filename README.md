\# Smart Campus API


\## Overview

REST API for managing rooms, sensors, and sensor readings using JAX-RS.


\## How to Run

1\. Build: `mvn clean install`

2\. Deploy WAR to Tomcat

3\. Access: `http://localhost:8080/smart-campus/api/v1/`

### Discovery
curl http://localhost:8080/smart-campus/api/v1/

### Create Room
curl -X POST http://localhost:8080/smart-campus/api/v1/rooms -H "Content-Type: application/json" -d "{\"name\":\"Lab\",\"building\":\"Engineering\"}"

### Get All Rooms
curl http://localhost:8080/smart-campus/api/v1/rooms

### Create Sensor
curl -X POST http://localhost:8080/smart-campus/api/v1/sensors -H "Content-Type: application/json" -d "{\"type\":\"CO2\",\"roomId\":\"1\",\"status\":\"ACTIVE\"}"

### Add Reading
curl -X POST http://localhost:8080/smart-campus/api/v1/sensors/1/readings -H "Content-Type: application/json" -d "{\"value\":450.5}"

### Filter Sensors
curl "http://localhost:8080/smart-campus/api/v1/sensors?type=CO2"


\## report answers 

Part 1: Service Architecture \& Setup

1.Project \& Application Configuration

Answer -  By default, JAX-RS creates a new instance of the resource class for each incoming request, not a singleton. This means that each request receives a fresh instance of 
RoomResource or SensorResource. In this implementation, the data storage maps for rooms and sensors are declared as static variables. Since static variables belong to the class itself rather than individual instances, they are shared across all requests. This creates a thread-safety concern, as multiple concurrent requests attempting to modify the static HashMap simultaneously can lead to race conditions and data corruption. To prevent this in a production environment, ConcurrentHashMap or synchronized keywords on data-modifying methods would be necessary. In this coursework, testing was sequential, so no race conditions occurred. 

2\. The ”Discovery” Endpoint

Answer - HATEOAS means the API response contains links to related resources. The discovery endpoint returns {"rooms": "/api/v1/rooms", "sensors": "/api/v1/sensors"}. This benefits client developers because they do not need to consult static documentation to determine subsequent API endpoints - the API provides this information directly. Instead of hardcoding URLs, clients can follow hypermedia links to discover URLs dynamically, reducing errors from manual URL construction. This approach also makes the API self-documenting and easier to navigate, while allowing the server to modify URL structures without breaking existing clients. 

Part 2: Room 

1\. Room Resource Implementation

Answer - Returning only room IDs consumes less network bandwidth but forces the client to make additional requests to retrieve details for each room. This creates the N+1 problem and increases latency. Returning full room objects uses more bandwidth initially but provides all data in a single response, eliminating extra round trips. For this campus API, full objects are returned because the expected number of rooms is small (hundreds), making the bandwidth difference negligible while simplifying and accelerating client-side processing. 

2\. Room Deletion \& Safety Logic

Answer - Yes, the DELETE operation is idempotent in this implementation. When a client sends a DELETE request for a room that exists, the room is deleted and a 204 No Content status is returned. If the same request is sent again, the room no longer exists, so a 404 Not Found status is returned. Although the status codes differ, the server reaches the same final state after both requests - the room is removed and no further damage occurs. Idempotency is defined by the effect on server state, not the HTTP response code. This enables clients to safely retry failed DELETE requests without negative consequences. 

Part 3: Sensor Operations \& Linking 

1\. Sensor Resource \& Integrity 

Answer - When a client sends data with a different Content-Type, such as text/plain or application/xml, to the @Consumes(APPLICATION_JSON) endpoint, JAX-RS automatically rejects the request before the resource method is invoked. The framework immediately returns an HTTP 415 Unsupported Media Type status code to the client. 
JAX-RS handles this validation entirely, eliminating the need for manual validation code. 

2\. Filtered Retrieval \& Search

Answer - Filtering using query parameters like ?type=CO2 is superior for several reasons. First, query parameters are semantically correct for optional criteria, whereas path parameters like /type/CO2 incorrectly imply that "CO2" is a specific resource rather than a filter value. 
Second, query parameters easily support multiple filters, such as 
?type=CO2&status=ACTIVE. Third, they are naturally optional - omitting the parameter returns all resources without requiring separate endpoint definitions. Path parameters require complex URL patterns for multiple filters and do not handle optionality effectively. 

Part 4: Deep Nesting with Sub – Resources

1\. The Sub-Resource Locator Pattern

Answer - The Sub-Resource Locator pattern organizes nested resource logic by delegating to separate dedicated classes. In this codebase, SensorResource contains a locator method that returns SensorReadingResource for the /sensors/{id}/readings path. This separation of concerns allows SensorResource to focus on sensor operations while SensorReadingResource manages reading operations. Without this pattern, all reading methods would reside within SensorResource, creating a bloated controller class that is difficult to maintain, test, and understand. This pattern improves code organization, reusability, and testability. 

Part 5: Advanced Error Handling, Exception Mapping \& Logging

2\. Dependency Validation (422 Unprocessable Entity)

Answer - HTTP 404 indicates that the requested resource or endpoint does not exist. In the sensor creation scenario, the /sensors endpoint exists and is reachable - the issue is not a missing endpoint but a missing roomId reference within an otherwise valid JSON payload. HTTP 422 Unprocessable Entity indicates that the request syntax is correct but the server cannot process it due to semantic errors or business rule violations. Therefore, 422 is more accurate as it communicates to the client: "Your request format is correct, but the room you referenced does not exist." 

4\. The Global Safety Net (500)

Answer - Exposing Java stack traces to external API consumers is dangerous because it reveals sensitive information that attackers can exploit. A stack trace exposes internal file paths (e.g., C:\project\src\...), library versions (e.g., Jersey 3.1.3), class names, method names, and line numbers. Attackers can use this information to identify known vulnerabilities in specific library versions and understand the server's internal structure to craft targeted attacks. The global exception mapper in this implementation prevents this by returning only a generic error message while logging the full stack trace internally. 

5\. API Request \& Response Logging Filters

Answer - Using JAX-RS filters for logging is superior to manual Logger.info() statements because filters centralize logging logic in a single location. Without filters, log statements would need to be added inside every resource method, violating the DRY principle and creating code duplication. Filters automatically intercept every request and response, logging the HTTP method, URI, and status code from a single class. This approach keeps resource classes focused on business logic rather than infrastructure concerns. 
 






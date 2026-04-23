\# Smart Campus API



\## Overview

REST API for managing rooms, sensors, and sensor readings using JAX-RS.



\## How to Run

1\. Build: `mvn clean install`

2\. Deploy WAR to Tomcat

3\. Access: `http://localhost:8080/smart-campus/api/v1/`



\## Sample curl Commands

\### Discovery

curl http://localhost:8080/smart-campus/api/v1/

\### Create Room

curl -X POST http://localhost:8080/smart-campus/api/v1/rooms -H "Content-Type: application/json" -d "{"name":"Lab","building":"Engineering"}"

\### Get All Rooms

curl http://localhost:8080/smart-campus/api/v1/rooms

\### Create Sensor

curl -X POST http://localhost:8080/smart-campus/api/v1/sensors -H "Content-Type: application/json" -d "{"type":"CO2","roomId":"1","status":"ACTIVE"}"

\### Add Reading

curl -X POST http://localhost:8080/smart-campus/api/v1/sensors/1/readings -H "Content-Type: application/json" -d "{"value":450.5}"

\### Filter Sensors

curl "http://localhost:8080/smart-campus/api/v1/sensors?type=CO2"

\## report answers 

Part 1: Service Architecture \& Setup

1.Project \& Application Configuration

Answer - By default, JAX-RS creates a new instance of the resource class for each incoming request, not a singleton. This means that each request gets a fresh instance of RoomResource or SensorResource . In my implementation, the data storage maps for rooms and sensors are declared as static variables. Since static variables are part of the class and not of each instance, they are shared by all requests. This creates a thread safety problem, because multiple requests trying to modify the static HashMap at the same time can lead to race conditions and data corruption. I would use ConcurrentHashMap or add synchronized keywords on methods that modify data, to avoid this in a production environment. No race conditions occurred in this coursework as testing was sequential.

2\. The ”Discovery” Endpoint

Answer - HATEOAS means the response of the API contains links to related resources. My discovery endpoint returns {"rooms": "/api/v1/rooms", "sensors": "/api/v1/sensors"} This helps client developers because they don’t have to read static documentation to know where to go next the API tells them directly. Instead of hardcoding URLs, clients can follow links to discover the URLs dynamically, reducing errors in constructing URLs manually. It also makes the API self-documenting and easier to navigate as it allows the server to modify the URL structures without breaking existing clients.

Part 2: Room 

1\. Room Resource Implementation

Answer - Returning just the room IDs uses less network bandwidth but requires the client to make more requests to get details about each room. This causes the N+1 problem and increases latency. Returning full room objects takes more bandwidth up front but gives you everything in one trip and avoids extra round trips. In my campus API, I chose to return whole objects. The bandwidth savings are negligible because we expect a small number of rooms (hundreds) , and it makes client processing simpler and faster.

2\. Room Deletion \& Safety Logic

Answer - Yes, in my implementation DELETE is idempotent. If the client sends a DELETE request for a room that exists, I delete it and return 204 No Content. If the same request is sent again, the room doesn't exist anymore so I return 404 Not Found. The status codes are different, but after both requests the server ends up in the same final state - the room is gone and no further damage occurs. Idempotency is defined by the effect on the state of the server , not the HTTP response code . This enables clients to properly retry failed DELETE requests without negative effects.

Part 3: Sensor Operations \& Linking 

1\. Sensor Resource \& Integrity 

Answer - When a client sends data with a different Content-Type such as text/plain or application/xml to my @Consumes(APPLICATION_JSON) endpoint, JAX-RS rejects the request automatically before my method is ever called. This causes the framework to immediately return an HTTP 415 Unsupported Media Type status code to the client. JAX-RS takes care of this entirely and I do not have to write any validation code for this.

2\. Filtered Retrieval \& Search

Answer - There are a few reasons why filtering using query parameters like ?type=CO2 is better. They are semantically correct for optional criteria. Path parameters such as /type/CO2 incorrectly suggest that "CO2" is a particular resource. Second, it is easy to combine multiple filters with query parameters (e.g., ?type=CO2&status=ACTIVE). Third, they are optional by nature – as they don’t need separate endpoint definitions, the parameter defaults to returning all resources. Complex url patterns for multiple filters and poor handling of optionality would be required for path parameters.

Part 4: Deep Nesting with Sub – Resources

1\. The Sub-Resource Locator Pattern

Answer - TThe Sub-Resource Locator pattern helps organize nested resource logic by using separate classes to manage resource logic. In my code, SensorResource includes a locator method that returns SensorReadingResource for the /sensors/{id}/readings path. This way, SensorResource focuses on sensor operations, while SensorReadingResource manages reading operations. If I didn't use this pattern, all reading methods would have end up in SensorResource, making it a large and difficult class to maintain, test, and understand. Using this pattern makes the code easier to organize, reuse, and test.

Part 5: Advanced Error Handling, Exception Mapping \& Logging

2\. Dependency Validation (422 Unprocessable Entity)

Answer - HTTP 404 means the requested resource or endpoint does not exist. In my sensor creation scenario, the /sensors endpoint is there and can be reached - the problem is not a missing endpoint but a missing roomId reference inside an otherwise valid JSON message. HTTP 422 Unprocessable Entity means the request is written correctly but the server cannot handle it because of meaning errors or business rule violations. Therefore, 422 is more accurate because it tells the client: "Your request format is fine, but the room you referenced does not exist.

4\. The Global Safety Net (500)

Answer - Exposing Java stack traces to external API consumers is dangerous because it reveals sensitive information that attackers can exploit. A stack trace exposes internal file paths (like C:\project\src\...), library versions (like Jersey 3.1.3), class names, method names, and line numbers. Attackers can use this information to identify known vulnerabilities in specific library versions and understand the server's internal structure to craft targeted attacks. My global exception mapper prevents this by returning only a generic error message while logging the full stack trace internally.

5\. API Request \& Response Logging Filters

Answer - Using JAX-RS filters for logging is better than manual Logger.info() statements because filters centralize logging logic in one place. Without filters, I would need to add log statements inside every resource method, which violates the DRY principle and creates code duplication. Filters automatically intercept every request and response, logging the HTTP method, URI, and status code from a single class. This keeps my resource classes focused on business logic rather than infrastructure concerns.





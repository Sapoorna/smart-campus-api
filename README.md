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

Answer - In JAX-RS, a new instance of the resource class is created for every incoming request by default. It is NOT a singleton.

This means each request gets its own fresh instance of RoomResource or SensorResource.

However, our data storage maps (rooms and sensors) are declared as static variables. Static variables belong to the class itself, not to individual instances. Therefore, all requests share the same data.

The challenge is thread-safety. When multiple requests try to modify the static HashMap simultaneously, race conditions can occur causing data corruption.

To prevent this, we could use ConcurrentHashMap or add synchronized keywords on methods that modify data. Since our testing was sequential, race conditions did not occur, but in production we would need proper synchronization.



2\. The ”Discovery” Endpoint

Answer - HATEOAS means the API response includes links to related resources. Our discovery endpoint returns {"rooms": "/api/v1/rooms", "sensors": "/api/v1/sensors"}.

This benefits client developers because they do not need to read static documentation to know where to go next. The API tells them directly.

Clients can discover URLs dynamically by following links rather than hardcoding them. This reduces errors from manual URL construction.

It also allows the server to change URL structures without breaking existing clients. The API becomes self-documenting and easier to navigate.



Part 2: Room 

1\. Room Resource Implementation

Answer- Returning only IDs uses less network bandwidth but forces the client to make additional requests to fetch each room's details. This is called the N+1 problem and increases latency.

Returning full room objects uses more bandwidth initially but provides all data immediately, eliminating extra requests.

For our campus API with a small number of rooms (hundreds), we chose to return full objects. The bandwidth difference is negligible and client-side processing is simpler and faster.



2\. Room Deletion \& Safety Logic

Answer- Yes, DELETE is idempotent in our implementation.

When the client sends the first DELETE request for a room, the room exists. We delete it and return 204 No Content.

When the client mistakenly sends the same DELETE request again, the room no longer exists. We return 404 Not Found.

Although the status codes differ, the final server state is identical after both requests: the room is gone. No additional damage is done by the second request.

Idempotency means clients can safely retry failed DELETE requests without causing harm.



Part 3: Sensor Operations \& Linking 

1\. Sensor Resource \& Integrity 

Answer - When a client sends data with a different Content-Type like text/plain or application/xml to our @Consumes(APPLICATION\_JSON) endpoint, JAX-RS automatically rejects the request before our method is ever called.

The framework immediately returns HTTP 415 Unsupported Media Type status code to the client.

We do not need to write any validation code for this. JAX-RS handles it completely for us.



2\. Filtered Retrieval \& Search

Answer - Query parameters like ?type=CO2 are superior for filtering for several reasons.

First, they are semantically correct for optional criteria, while path parameters like /type/CO2 incorrectly imply that "CO2" is a specific resource.

Second, query parameters support multiple filters easily, such as ?type=CO2\&status=ACTIVE.

Third, they are naturally optional. Omitting the parameter returns all resources without needing separate endpoint definitions.

Path parameters would require complex URL patterns for multiple filters and do not handle optionality well.



Part 4: Deep Nesting with Sub – Resources

1\. The Sub-Resource Locator Pattern

Answer - The Sub-Resource Locator pattern delegates nested resource logic to separate dedicated classes.

In our code, SensorResource has a locator method that returns SensorReadingResource for the /sensors/{id}/readings path.

This separates concerns: SensorResource handles sensor operations, while SensorReadingResource handles reading operations.

Without this pattern, all reading methods would be inside SensorResource, creating a bloated "God class" that is hard to maintain, test, and understand.

The pattern improves code organization, reusability, and testability.



Part 5: Advanced Error Handling, Exception Mapping \& Logging

2\. Dependency Validation (422 Unprocessable Entity)

Answer - HTTP 404 means the requested resource or endpoint itself does not exist.

In our sensor creation scenario, the /sensors endpoint does exist and is reachable. The problem is not a missing endpoint.

The problem is a missing roomId reference inside an otherwise valid JSON payload.

HTTP 422 Unprocessable Entity specifically means the request syntax is correct but the server cannot process it due to semantic errors or business rule violations.

Therefore, 422 is more accurate because it tells the client: "Your request format is fine, but the room you referenced does not exist."



4\. The Global Safety Net (500)

Answer - Exposing Java stack traces to external API consumers is dangerous because it reveals sensitive information attackers can exploit.

A stack trace exposes internal file paths like C:\\project\\src\\..., library versions like Jersey 3.1.3, class names, method names, and line numbers.

Attackers can use this information to identify known vulnerabilities in specific library versions.

They can also understand the server's internal structure and craft targeted attacks.

Our global exception mapper prevents this by returning only a generic error message while logging the full stack trace internally.



5\. API Request \& Response Logging Filters

Answer - Using JAX-RS filters for logging is better than manual Logger.info() statements for several reasons.

First, filters centralize logging logic in one place. Without filters, we would need to add log statements inside every resource method.

Second, this violates the DRY principle and creates code duplication.

Third, filters automatically intercept every request and response, logging the HTTP method, URI, and status code from a single class.

Fourth, filters keep resource classes focused on business logic rather than infrastructure concerns






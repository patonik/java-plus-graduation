
# Project Documentation

## Overview
This project implements a microservices-based architecture for an event and user management system. 
The system has been modularized to improve scalability, fault tolerance, and maintainability. 
Key services, including `user-service`, `event-service`, and `request-service`, have been decoupled to run independently 
and communicate via REST APIs.

The system also includes an **API Gateway** that routes requests to the appropriate services, 
a **Discovery Server** (Eureka) for service registration and discovery, 
and a **Config Server** for centralized configuration management.

## Architecture

### Microservices
The project follows a **microservices architecture** where each service is responsible for a specific domain. 
These are the main services:

1. **User Service (`user-service`)**
    - Manages user data, including registration, authentication, and user information.
    - Owns its own database (`user-db`).
    - Exposes APIs for user-related operations (e.g., getting user details, creating users).
    - Configurations are stored in `config-server` and accessed using Spring Cloud Config.

2. **Event Service (`event-service`)**
    - Handles events, including creation, updates, and management.
    - Owns its own database (`event-db`).
    - Exposes APIs for event-related operations (e.g., retrieving events, updating events).
    - Configurations are fetched from `config-server`.

3. **Request Service (`request-service`)**
    - Manages participation requests for events.
    - Owns its own database (`request-db`).
    - Exposes APIs for managing requests (e.g., creating, canceling, and updating requests).
    - Configurations are fetched from `config-server`.

### Shared Components
1. **API Gateway (`gateway-server`)**
    - Acts as a reverse proxy to route requests to the appropriate service.
    - Handles routing, load balancing, and request filtering.
    - Exposes a unified API for external clients to interact with the system.
    - Configures dynamic routing using Spring Cloud Gateway and Eureka for service discovery.

2. **Discovery Server (`discovery-server`)**
    - Provides service registration and discovery using Spring Cloud Eureka.
    - Enables communication between services without hardcoding the URLs, improving flexibility and scalability.

3. **Config Server (`config-server`)**
    - Centralized configuration management using Spring Cloud Config.
    - All services retrieve their configurations from this server, ensuring consistency across all services.

### Database Setup
Each service has its own isolated database, ensuring **data isolation** and **independence**. 
The services communicate with each other through REST APIs, avoiding tight coupling.

- `user-service` connects to `ewm-user`
- `event-service` connects to `ewm-event`
- `request-service` connects to `ewm-request`

### Service Communication
- **Internal Communication:** Services communicate with each other via REST APIs. For example, `event-service` communicates with `request-service` to manage requests for specific events, and `user-service` handles user-related data.
- **External Communication:** External clients communicate with the system through the **API Gateway**. The Gateway routes the requests to the appropriate service based on the path.

---

## Configuration
The configuration for each service is managed centrally by **Spring Cloud Config**. Configurations are stored locally and pulled by the services during runtime.

### Key Configuration Files
1. **Config Server (`config-server`)**
    - The configuration server retrieves configuration properties for all services.
    - Configuration properties are stored in `classpath:/config/{application}` directories.

2. **Service-Specific Configurations**
    - `user-service`, `event-service`, and `request-service` retrieve their configurations from the Config Server based on the active profile.

---

## Internal API

### Service Endpoints
Here are the key internal API endpoints for communication between services.

#### 1. **User Service API** (`user-service`)
- **GET /users/{userId}** - Get user details.
- **POST /users** - Create a new user.
- **DELETE /users/{userId}** - Delete a user.

#### 2. **Event Service API** (`event-service`)
- **GET /events** - Get events based on filters.
- **POST /users/{userId}/events** - Create a new event for a user.
- **PATCH /events/{eventId}** - Update event details.

#### 3. **Request Service API** (`request-service`)
- **GET /users/{userId}/requests** - Get all requests for a user.
- **POST /users/{userId}/requests** - Add a new participation request.
- **PATCH /requests/{requestId}/cancel** - Cancel a participation request.

### Feign Client Integration
For inter-service communication, **Feign Clients** are used. Each service exposes an API that is consumed by other services via Feign clients. For example:
- `event-service` can call `user-service` to get user information.
- `request-service` can call `event-service` to manage participation requests.

### External API

The **External API** exposed by the **API Gateway** can be accessed by clients. The routes defined in the API Gateway configuration route external requests to the appropriate microservices.

#### External Routes
- **POST /users** - To create a new user (calls `user-service`).
- **GET /events** - To get a list of events (calls `event-service`).
- **POST /users/{userId}/requests** - To create a participation request (calls `request-service`).

You can find the full specification of the external API for main services [here](https://github.com/patonik/java-plus-graduation/blob/main/ewm-main-service-spec.json).
The statistical service is described [here](https://github.com/patonik/java-plus-graduation/blob/main/ewm-stats-service-spec.json).

---

## Running the Application

1. **Docker Compose**: All services, databases, and external dependencies can be managed using `docker-compose`. Ensure all services are connected to the correct Docker network (`ewm-net`).
2. **Start the Application**: Run the application using the following commands:
   ```bash
   docker-compose up --build
   ```

This will start the **discovery-server**, **config-server**, **gateway-server**, **user-service**, **event-service**, and **request-service** in separate containers, all interconnected.

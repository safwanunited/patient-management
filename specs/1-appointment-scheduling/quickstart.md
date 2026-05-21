# Quickstart: Appointment Scheduling and Retrieval

**Feature**: `1-appointment-scheduling`  
**Date**: 2026-05-21

---

## Prerequisites

- Java 21
- Maven 3.9+
- Docker (for Testcontainers integration tests and optional local PostgreSQL)
- The service starts on port **4000** (configured in `application.properties`)

---

## 1. Add Testcontainers to pom.xml

Before running integration tests, add the following dependencies to `pom.xml`
(Spring Boot 3.x BOM manages the version):

```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 2. Build and Run

```bash
# Build (skip tests for a fast check)
./mvnw clean package -DskipTests

# Start the service (uses H2 in-memory by default)
./mvnw spring-boot:run
```

The service starts at `http://localhost:4000`.
Interactive API docs (Swagger UI) are at `http://localhost:4000/swagger-ui.html`.
OpenAPI JSON spec is at `http://localhost:4000/v3/api-docs`.

---

## 3. Run Tests

```bash
# Unit tests only (no Docker required)
./mvnw test -Dtest="AppointmentTest,AppointmentServiceTest,AppointmentControllerTest"

# All tests including integration (Docker must be running for Testcontainers)
./mvnw verify
```

---

## 4. Example Requests

### Book an Appointment

```bash
curl -s -X POST http://localhost:4000/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": "<UUID of an ACTIVE patient>",
    "doctorId":  "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "appointmentStart": "2026-06-01T10:00:00"
  }' | jq .
```

Expected response — `201 Created`:

```json
{
  "id": "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "patientId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "doctorId":  "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "appointmentStart": "2026-06-01T10:00:00",
  "appointmentEnd":   "2026-06-01T10:30:00",
  "status": "SCHEDULED",
  "createdAt": "2026-05-21T09:00:00",
  "updatedAt": "2026-05-21T09:00:00"
}
```

### Get Appointment Details

```bash
curl -s http://localhost:4000/appointments/d290f1ee-6c54-4b01-90e6-d701748f0851 | jq .
```

Expected response — `200 OK`: full appointment record (same shape as above).

### Cancel an Appointment

```bash
curl -s -X PATCH \
  http://localhost:4000/appointments/d290f1ee-6c54-4b01-90e6-d701748f0851/cancel | jq .
```

Expected response — `200 OK`:

```json
{
  "id": "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "status": "CANCELLED",
  "updatedAt": "2026-05-21T09:15:00",
  ...
}
```

### Reschedule an Appointment

```bash
curl -s -X PATCH \
  http://localhost:4000/appointments/d290f1ee-6c54-4b01-90e6-d701748f0851/reschedule \
  -H "Content-Type: application/json" \
  -d '{ "newAppointmentStart": "2026-06-02T14:00:00" }' | jq .
```

Expected response — `200 OK`:

```json
{
  "id": "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "appointmentStart": "2026-06-02T14:00:00",
  "appointmentEnd":   "2026-06-02T14:30:00",
  "status": "SCHEDULED",
  "updatedAt": "2026-05-21T09:20:00",
  ...
}
```

---

## 5. Key Error Scenarios to Verify

| Scenario | Expected HTTP |
|----------|--------------|
| `appointmentStart` in the past | `400 Bad Request` |
| Patient ID is INACTIVE | `422 Unprocessable Entity` |
| Doctor already booked at that time | `409 Conflict` |
| Unknown appointment ID | `404 Not Found` |
| Cancel an already-CANCELLED appointment | `409 Conflict` |
| Reschedule a CANCELLED appointment | `422 Unprocessable Entity` |

---

## 6. Source Paths Quick Reference

| Layer | Package / Path |
|-------|---------------|
| Domain model | `com.pm.patient_service.domain.model` |
| Inbound ports | `com.pm.patient_service.application.port.in` |
| Outbound ports | `com.pm.patient_service.application.port.out` |
| Use case service | `com.pm.patient_service.service.AppointmentService` |
| REST adapter | `com.pm.patient_service.controller.AppointmentController` |
| JPA adapter | `com.pm.patient_service.infrastructure.persistence` |
| DTOs | `com.pm.patient_service.dto` |
| Mapper | `com.pm.patient_service.mapper.AppointmentMapper` |
| Exceptions | `com.pm.patient_service.exception` |

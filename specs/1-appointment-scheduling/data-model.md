# Data Model: Appointment Scheduling and Retrieval

**Feature**: `002-appointment-scheduling`  
**Date**: 2026-05-21

---

## Entities

### Appointment (Domain Entity)

The core aggregate root of this feature. Represents a confirmed time block between
a patient and a doctor. This object is a **pure Java class** — no JPA, Spring, or
validation framework annotations are present.

| Field | Type | Rules |
|-------|------|-------|
| `id` | `UUID` | Immutable; generated at creation; never null |
| `patientId` | `UUID` | Required; must reference an ACTIVE patient at creation and reschedule time |
| `doctorId` | `UUID` | Required; opaque reference; existence validated by calling service |
| `appointmentStart` | `LocalDateTime` | Required; must be strictly after "now" at creation/reschedule time |
| `appointmentEnd` | `LocalDateTime` | Derived: always `appointmentStart + 30 minutes`; never supplied by client |
| `status` | `AppointmentStatus` | Required; default `SCHEDULED` at creation |
| `createdAt` | `LocalDateTime` | Immutable audit field; set at creation |
| `updatedAt` | `LocalDateTime` | Mutable; updated on cancel and reschedule |

**Invariants enforced by the domain model**:

1. `appointmentEnd` is always exactly 30 minutes after `appointmentStart`.
2. `status` transitions only via explicit methods: `cancel()` and `reschedule(newStart)`.
3. Calling `cancel()` on a non-SCHEDULED appointment throws `IllegalStateException`.
4. Calling `reschedule()` on a non-SCHEDULED appointment throws `IllegalStateException`.

---

### AppointmentStatus (Enum)

```
SCHEDULED  ──── cancel()       ──→  CANCELLED
SCHEDULED  ──── reschedule()   ──→  SCHEDULED (at new time)
CANCELLED  ──── cancel()       ──→  IllegalStateException (422 at HTTP layer)
CANCELLED  ──── reschedule()   ──→  IllegalStateException (422 at HTTP layer)
```

Valid values: `SCHEDULED`, `CANCELLED`

---

## Persistence Entity

`AppointmentJpaEntity` is a **separate class** in `infrastructure/persistence/`.
It mirrors the domain fields but carries all JPA annotations (`@Entity`, `@Column`,
`@Enumerated`, `@Version`). The mapper converts between domain and JPA entity —
no JPA class ever escapes the infrastructure layer.

| JPA Column | Maps to Domain Field | Notes |
|------------|---------------------|-------|
| `id` (UUID, PK) | `Appointment.id` | `@GeneratedValue` |
| `patient_id` (UUID, NOT NULL) | `Appointment.patientId` | FK reference; no cascade |
| `doctor_id` (UUID, NOT NULL) | `Appointment.doctorId` | Opaque; no FK constraint |
| `appointment_start` (TIMESTAMP, NOT NULL) | `Appointment.appointmentStart` | Indexed |
| `appointment_end` (TIMESTAMP, NOT NULL) | `Appointment.appointmentEnd` | Indexed with `doctor_id` for overlap query |
| `status` (VARCHAR(20), NOT NULL) | `Appointment.status` | `@Enumerated(STRING)` |
| `created_at` (TIMESTAMP, NOT NULL) | `Appointment.createdAt` | `updatable = false` |
| `updated_at` (TIMESTAMP, NOT NULL) | `Appointment.updatedAt` | Updated on cancel/reschedule |
| `version` (BIGINT) | — | `@Version`; optimistic lock baseline |

**Composite index** on `(doctor_id, appointment_start, appointment_end)` to support
the overlap detection query efficiently.

---

## Port Interfaces (Application Layer Contracts)

### Inbound Ports (`application/port/in/`)

```java
// BookAppointmentUseCase
AppointmentResponseDTO book(AppointmentRequestDTO request);

// GetAppointmentUseCase
AppointmentResponseDTO getById(UUID id);

// CancelAppointmentUseCase
AppointmentResponseDTO cancel(UUID id);

// RescheduleAppointmentUseCase
AppointmentResponseDTO reschedule(UUID id, RescheduleRequestDTO request);
```

### Outbound Ports (`application/port/out/`)

```java
// AppointmentRepositoryPort
Appointment save(Appointment appointment);
Optional<Appointment> findById(UUID id);
boolean hasOverlap(UUID doctorId, LocalDateTime start, LocalDateTime end, UUID excludeId);
// excludeId is null for new bookings; set to the appointment's own ID when rescheduling

// PatientValidationPort
boolean isPatientActive(UUID patientId);
```

---

## Validation Rules (Application Layer — NOT in domain model)

| Rule | Source | HTTP response on violation |
|------|--------|--------------------------|
| `patientId`, `doctorId`, `appointmentStart` all non-null | Jakarta Validation on DTO | 400 Bad Request |
| `appointmentStart` is in the future | `AppointmentService` | 400 Bad Request |
| Patient with `patientId` exists and has ACTIVE status | `PatientValidationPort` | 422 Unprocessable Entity |
| No SCHEDULED appointment overlaps `[start, start+30min)` for same `doctorId` | `AppointmentRepositoryPort.hasOverlap()` | 409 Conflict |
| Appointment exists for given `id` | `AppointmentRepositoryPort.findById()` | 404 Not Found |
| `status == SCHEDULED` before cancel | Domain invariant | 409 Conflict |
| `status == SCHEDULED` before reschedule | Domain invariant | 422 Unprocessable Entity |
| `newAppointmentStart` is in the future (reschedule) | `AppointmentService` | 400 Bad Request |

---

## Relationships to Existing Entities

```
Patient (existing, com.pm.patient_service.model)
  └── referenced by Appointment.patientId (UUID FK, no cascade, no @ManyToOne)
      PatientValidationAdapter reads PatientRepository.findById() to check status

Doctor (external reference only)
  └── referenced by Appointment.doctorId (UUID, no FK constraint in this service)
      No doctor entity is owned by this service
```

---

## DTO Shapes

### AppointmentRequestDTO (POST /appointments)

```json
{
  "patientId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "doctorId":  "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "appointmentStart": "2026-06-01T10:00:00"
}
```

### RescheduleRequestDTO (PATCH /appointments/{id}/reschedule)

```json
{
  "newAppointmentStart": "2026-06-02T14:00:00"
}
```

### AppointmentResponseDTO (all successful responses)

```json
{
  "id":               "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "patientId":        "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "doctorId":         "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "appointmentStart": "2026-06-01T10:00:00",
  "appointmentEnd":   "2026-06-01T10:30:00",
  "status":           "SCHEDULED",
  "createdAt":        "2026-05-21T09:00:00",
  "updatedAt":        "2026-05-21T09:00:00"
}
```

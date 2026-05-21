# Research: Appointment Scheduling and Retrieval

**Feature**: `002-appointment-scheduling`  
**Date**: 2026-05-21  
**Status**: Complete — all NEEDS CLARIFICATION items resolved

---

## Decision 1: Cancellation Endpoint Design

**Decision**: `PATCH /appointments/{id}/cancel`

**Rationale**: A sub-resource action path makes the state transition explicit and
unambiguous in the URL, avoiding confusion with `PATCH /appointments/{id}` (used
for rescheduling). It preserves the appointment record in the response body, which
is essential for audit trails in clinical systems. `DELETE` was rejected because
appointments have historical and compliance value — they should never be physically
removed.

**Alternatives considered**:
- `DELETE /appointments/{id}` — rejected: physically deletes the record; loses status
  history needed for audit.
- `PATCH /appointments/{id}` with `{ "status": "CANCELLED" }` — rejected: a generic
  PATCH body is ambiguous about which fields are mutable and opens the endpoint to
  unintended state mutations.

---

## Decision 2: Appointment Duration and Overlap Boundary

**Decision**: Fixed 30-minute system-wide constant — `AppointmentDuration.DEFAULT_MINUTES = 30`.
Adjacent appointments (e.g., 10:00–10:30 immediately followed by 10:30–11:00) are
NOT considered overlapping; overlap requires the intervals to properly intersect
(`start_A < end_B AND end_A > start_B`).

**Rationale**: Simplest approach for V1; doctor profiles are not owned by this
service, making per-doctor configuration impractical without an additional API
dependency. An explicit `durationMinutes` field adds complexity for negligible
benefit at this scale. The named constant is easily changed when requirements evolve.

**Alternatives considered**:
- Configurable per doctor/specialty — rejected: requires inter-service data about
  doctor profiles that this service does not own.
- Explicit `durationMinutes` in request body — rejected: over-engineers V1; can be
  added as a non-breaking field addition later.

---

## Decision 3: Patient Validation Strategy

**Decision**: `PatientValidationPort` outbound interface implemented by
`PatientValidationAdapter`, which queries the existing `PatientRepository` directly
(same database, same JVM). No HTTP inter-service call required.

**Rationale**: Patient data lives in the same Spring Boot application and the same
PostgreSQL instance. Wrapping the access through a port interface keeps
`AppointmentService` isolated from the JPA layer while avoiding the latency,
failure modes, and complexity of an HTTP call to a separate patient service.

**Alternatives considered**:
- Direct `PatientRepository` injection into `AppointmentService` — rejected:
  violates hexagonal architecture (infrastructure import in application layer).
- HTTP call to a separate patient microservice — rejected: the current codebase is
  a single service; no separate patient API exists.

---

## Decision 4: Concurrent Double-Booking Prevention

**Decision**: Use `@Transactional` with `LockModeType.PESSIMISTIC_WRITE` on the
overlap-check query inside `AppointmentPersistenceAdapter`. The lock prevents
concurrent transactions from reading the same doctor's schedule as conflict-free
simultaneously before either inserts. Combined with `@Transactional` on
`AppointmentService.book()`, this guarantees that two simultaneous requests for the
same doctor slot result in exactly one success and one 409 Conflict.

**Rationale**: Pessimistic write lock is the correct tool when conflicting writes
are anticipated under concurrent load. Optimistic locking (`@Version`) protects
against stale updates but not against concurrent inserts that both pass an
overlap check before either commits.

**Alternatives considered**:
- Optimistic locking with `@Version` on the entity — rejected: does not prevent
  two concurrent transactions both reading a clean schedule and both inserting.
- `SERIALIZABLE` transaction isolation — rejected: excessive performance penalty;
  serialization failures require client-side retry logic.
- Database unique constraint on (doctorId, appointmentStart) — partially useful
  but does not handle overlapping intervals (only exact-start conflicts).

---

## Decision 5: Testcontainers Integration Test Setup

**Decision**: Add `org.testcontainers:junit-jupiter` and
`org.testcontainers:postgresql` to `pom.xml` (test scope). Spring Boot 3.x
manages Testcontainers versions via the parent BOM — no explicit version required.

**Required pom.xml additions**:
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

**Rationale**: `AppointmentPersistenceAdapterTest` must verify the JPQL
overlap-detection query against a real PostgreSQL dialect. H2 has subtle SQL
dialect differences that would give false confidence. Testcontainers spins up
an ephemeral PostgreSQL container for each test class, matching the production
database engine exactly.

---

## Decision 6: Reschedule Endpoint Design

**Decision**: `PATCH /appointments/{id}/reschedule` with body `{ "newAppointmentStart": "ISO-8601 datetime" }`.

**Rationale**: Mirrors the cancel sub-resource action pattern for symmetry. The
minimal body (only the new start time) keeps the contract simple while applying
all existing validation rules (future date, conflict detection, duration calculation).
`appointmentEnd` is always recomputed as `newStart + 30 min`; it is not accepted
as input to prevent clients from supplying inconsistent intervals.

**Alternatives considered**:
- `PUT /appointments/{id}` with full body — rejected: PUT implies full replacement,
  which would require the client to re-supply all fields and could accidentally
  overwrite status.
- `PATCH /appointments/{id}` with generic body — rejected: same ambiguity concern
  as for cancellation.

---

## Summary Table

| # | Question | Resolved Decision |
|---|----------|------------------|
| 1 | Cancellation endpoint | `PATCH /appointments/{id}/cancel` |
| 2 | Appointment duration | Fixed 30 min (`AppointmentDuration.DEFAULT_MINUTES`) |
| 3 | Patient validation approach | `PatientValidationPort` → `PatientValidationAdapter` → `PatientRepository` |
| 4 | Concurrent booking safety | `PESSIMISTIC_WRITE` lock on overlap-check query |
| 5 | Integration test database | Testcontainers PostgreSQL (add to pom.xml) |
| 6 | Reschedule endpoint | `PATCH /appointments/{id}/reschedule` with `newAppointmentStart` body |

# Feature Specification: Appointment Scheduling and Retrieval

**Feature Branch**: `1-appointment-scheduling`  
**Created**: 2026-05-21  
**Status**: Draft  
**Input**: User description: "As a clinic receptionist, I want to seamlessly book, reschedule, cancel, and retrieve patient appointments with specific doctors. This ensures the clinic schedule remains optimized and conflict-free, allowing us to provide patients with timely care and reduce administrative overhead."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Book a Patient Appointment (Priority: P1)

A clinic receptionist selects an active patient and a target doctor, provides a
future date and time, and submits a booking request. The system validates the
patient's active status, confirms the date is in the future, checks that the
doctor has no conflicting appointment in the requested time window, and — if all
checks pass — creates the appointment with a SCHEDULED status, returning the full
appointment record.

**Why this priority**: Booking is the foundational operation. Without it, no
other appointment lifecycle action (retrieve, cancel, reschedule) is possible.
It also encodes the most business-critical rules: conflict prevention, patient
validation, and chronological integrity.

**Independent Test**: Can be fully tested by submitting a POST /appointments
request with valid data and verifying a 201 Created response containing a
SCHEDULED appointment; conflict rules tested by submitting a duplicate booking
for the same doctor slot and verifying 409 Conflict.

**Acceptance Scenarios**:

1. **Given** an active patient and a doctor with no appointment at the requested
   time, **When** a receptionist submits a booking for a future date/time,
   **Then** the system creates the appointment, assigns SCHEDULED status, and
   returns 201 Created with the full appointment record.
2. **Given** a doctor who already holds an appointment that overlaps the
   requested time window, **When** a receptionist submits a booking,
   **Then** the system returns 409 Conflict and does not create the appointment.
3. **Given** a patient whose status is not ACTIVE (e.g., INACTIVE or DEACTIVATED),
   **When** a receptionist submits a booking, **Then** the system returns
   422 Unprocessable Entity with a clear reason.
4. **Given** a requested appointment date/time that is in the past or exactly
   equal to the current moment, **When** a receptionist submits a booking,
   **Then** the system returns 400 Bad Request.
5. **Given** a booking request missing any required field (patient ID, doctor ID,
   or appointment date/time), **When** submitted, **Then** the system returns
   400 Bad Request identifying the missing field(s).

---

### User Story 2 - Retrieve Appointment Details (Priority: P2)

A clinic receptionist looks up an appointment by its unique identifier to review
full scheduling details — patient, doctor, date/time, and status.

**Why this priority**: Retrieval is required immediately after booking to confirm
the record and throughout the day for reference. It is a prerequisite for any
future cancel or reschedule workflow.

**Independent Test**: Can be fully tested by creating an appointment (US1) and
then calling GET /appointments/{id} with the returned ID, verifying that all
fields in the response match the booking input.

**Acceptance Scenarios**:

1. **Given** a valid, existing appointment ID, **When** a receptionist requests
   the appointment details, **Then** the system returns 200 OK with the complete
   appointment record (patient ID, doctor ID, date/time, status, and metadata).
2. **Given** an appointment ID that does not exist, **When** the receptionist
   requests details, **Then** the system returns 404 Not Found.
3. **Given** a malformed appointment ID (e.g., non-numeric or invalid format),
   **When** the receptionist requests details, **Then** the system returns
   400 Bad Request.

---

### User Story 3 - Cancel an Appointment (Priority: P3)

A clinic receptionist cancels an existing SCHEDULED appointment, making the
doctor's time slot available for other patients and updating the appointment
record to reflect CANCELLED status.

**Why this priority**: Cancellation frees capacity and is a core lifecycle
transition for any scheduling system. Without it, slots remain blocked by
no-shows or patient-initiated cancellations, degrading schedule quality.

**Independent Test**: Can be fully tested by booking an appointment (US1),
cancelling it, and verifying the status transitions to CANCELLED; attempting to
cancel an already-cancelled appointment should be rejected.

**Acceptance Scenarios**:

1. **Given** an existing SCHEDULED appointment, **When** the receptionist
   cancels it, **Then** the system transitions status to CANCELLED and returns
   200 OK with the updated record.
2. **Given** an appointment already in CANCELLED status, **When** the
   receptionist attempts cancellation again, **Then** the system returns
   409 Conflict or 422 Unprocessable Entity indicating the transition is invalid.
3. **Given** a non-existent appointment ID, **When** the receptionist cancels,
   **Then** the system returns 404 Not Found.

[NEEDS CLARIFICATION: The HTTP method and path for cancellation were not
specified. Options are: (A) DELETE /appointments/{id}, (B) PATCH
/appointments/{id} with a status body, or (C) POST
/appointments/{id}/cancel. Please confirm the preferred REST convention for
this service.]

---

### User Story 4 - Reschedule an Appointment (Priority: P4)

A clinic receptionist moves an existing SCHEDULED appointment to a new future
date/time, applying all the same conflict and validation rules as a new booking
and transitioning the record to reflect the updated time.

**Why this priority**: Rescheduling is a frequent operation in clinical settings.
Without it, receptionists must cancel and re-book, losing continuity in the
appointment record.

**Independent Test**: Can be fully tested by booking an appointment (US1),
rescheduling it to a conflict-free future slot, and verifying the date/time
updates and status remains SCHEDULED; attempting to reschedule to a conflicting
slot should return 409 Conflict.

**Acceptance Scenarios**:

1. **Given** a SCHEDULED appointment and a new future date/time with no doctor
   conflict, **When** the receptionist submits a reschedule request, **Then** the
   system updates the appointment date/time, keeps status as SCHEDULED, and
   returns 200 OK with the updated record.
2. **Given** a SCHEDULED appointment and a new date/time that conflicts with
   another appointment for the same doctor, **When** the receptionist submits a
   reschedule, **Then** the system returns 409 Conflict.
3. **Given** a CANCELLED appointment, **When** the receptionist attempts to
   reschedule, **Then** the system returns 422 Unprocessable Entity.
4. **Given** a reschedule to a past date/time, **When** submitted, **Then** the
   system returns 400 Bad Request.

[NEEDS CLARIFICATION: Appointment duration determines the overlap window used in
conflict detection. Should duration be (A) a fixed system-wide constant (e.g.,
30 minutes), (B) configurable per doctor or specialty, or (C) specified
explicitly by the receptionist at booking time? This also affects whether
adjacently-timed appointments (e.g., 10:00–10:30 and 10:30–11:00) are
considered conflicting.]

---

### Edge Cases

- Two simultaneous booking requests for the same doctor/time slot arriving
  concurrently must not both succeed (race condition / optimistic locking).
- Booking a slot for a patient who is deactivated between the time the
  receptionist opens the form and submits it.
- Requesting appointment details with a valid ID format but for a different
  tenant/clinic (if multi-tenancy is in scope).
- Rescheduling an appointment to the same date/time as the original (no-op
  or explicit rejection).
- Extremely large appointment IDs or injection-style inputs in path parameters.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST expose a POST /appointments endpoint that creates a
  new appointment record and returns 201 Created with the full appointment details.
- **FR-002**: System MUST validate that the referenced patient exists and has an
  ACTIVE status before creating an appointment; non-ACTIVE patients MUST result
  in 422 Unprocessable Entity.
- **FR-003**: System MUST validate that the requested appointment date/time is
  strictly in the future; past or present timestamps MUST result in 400 Bad Request.
- **FR-004**: System MUST detect scheduling overlaps for the target doctor within
  the appointment's time window and return 409 Conflict when a conflict exists.
- **FR-005**: System MUST assign SCHEDULED status to every successfully created
  appointment at the time of creation.
- **FR-006**: System MUST expose a GET /appointments/{id} endpoint that returns
  the full appointment record for a valid, existing ID with 200 OK.
- **FR-007**: System MUST return 404 Not Found when GET /appointments/{id} is
  called with an ID that does not correspond to any appointment.
- **FR-008**: System MUST return 400 Bad Request for any request that is missing
  required fields or contains structurally invalid input.
- **FR-009**: System MUST support cancelling a SCHEDULED appointment, transitioning
  its status to CANCELLED; attempting to cancel a non-SCHEDULED appointment MUST
  be rejected. [NEEDS CLARIFICATION: cancellation endpoint method and path — see
  User Story 3.]
- **FR-010**: System MUST support rescheduling a SCHEDULED appointment to a new
  future date/time, applying all conflict-detection and date-validation rules;
  rescheduling a CANCELLED appointment MUST be rejected with 422.
- **FR-011**: System MUST persist all appointment mutations (create, cancel,
  reschedule) durably so that a subsequent GET /appointments/{id} reflects the
  latest state.

### Key Entities

- **Appointment**: Represents a scheduled meeting between a patient and a doctor.
  Attributes: unique identifier, patient reference (ID), doctor reference (ID),
  appointment start date/time, appointment duration or end date/time, status
  (SCHEDULED | CANCELLED), creation timestamp, last-updated timestamp.
- **AppointmentStatus**: The lifecycle state of an appointment. Valid transitions:
  SCHEDULED → CANCELLED (cancel), SCHEDULED → SCHEDULED (reschedule with new time).
- **Patient** (read reference only): An active patient whose identity is validated
  at booking time. Sourced from the existing patient data managed by the Patient
  service; this feature does NOT own patient data.
- **Doctor** (reference): The practitioner assigned to the appointment. Identified
  by a doctor ID; the appointment service validates existence but does not own
  doctor profile data.

### Assumptions

- A patient's ACTIVE/INACTIVE status is determined at booking and reschedule time;
  a patient deactivated after booking does not retroactively invalidate existing
  appointments.
- Adjacent appointments for the same doctor (end time of one equals start time of
  next) are NOT considered conflicting — overlap requires the intervals to intersect.
- Appointment duration defaults to 30 minutes unless the clarification on duration
  (see US4 NEEDS CLARIFICATION) resolves otherwise.
- Doctor IDs are valid opaque identifiers maintained externally; the appointment
  service validates their presence but does not manage doctor profiles.
- The service is single-clinic (no multi-tenancy) for this feature iteration.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A receptionist can complete an end-to-end appointment booking in
  under 30 seconds from submitting the request to receiving confirmation.
- **SC-002**: 100% of booking and rescheduling attempts that would create a doctor
  overlap are rejected with 409 Conflict — zero double-bookings reach persisted
  state.
- **SC-003**: Appointment details are retrievable within 1 second for any valid ID
  under normal operating conditions.
- **SC-004**: All invalid requests (past dates, inactive patients, missing fields,
  unknown IDs) return an appropriate HTTP error code with a human-readable reason
  in 100% of cases.
- **SC-005**: Status transitions (SCHEDULED → CANCELLED) are reflected immediately
  in subsequent GET /appointments/{id} responses with no eventual-consistency lag.
- **SC-006**: The full appointment lifecycle — book, retrieve, cancel, reschedule —
  is exercisable without manual data correction, achieving a task-completion rate
  of 95% or above in receptionist workflow testing.

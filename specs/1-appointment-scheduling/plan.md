# Implementation Plan: Appointment Scheduling and Retrieval

**Branch**: `1-appointment-scheduling` | **Date**: 2026-05-21 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/1-appointment-scheduling/spec.md`

## Summary

Extend the existing `patient-service` Spring Boot application with a fully
hexagonal appointment scheduling subsystem. The feature exposes four REST
endpoints — book, retrieve, cancel, and reschedule — with server-side overlap
detection, patient-status validation, and a clean domain model that contains
zero framework annotations. All four use-case flows are driven through inbound
port interfaces; persistence and patient validation are accessed exclusively
through outbound port interfaces implemented in a new infrastructure layer.

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 3.5.8 (Web, Data JPA, Validation), SpringDoc OpenAPI 2.7.0, H2 (dev/test), PostgreSQL (runtime), Testcontainers (integration tests — to be added to pom.xml)  
**Storage**: H2 in-memory (development), PostgreSQL (production); schema via JPA DDL (`ddl-auto=update`; Flyway migration recommended as follow-up)  
**Testing**: JUnit 5 + Mockito (unit), Spring Boot Test + Testcontainers PostgreSQL (adapter/integration), `@WebMvcTest` (controller slice)  
**Target Platform**: Linux server (Docker container, same Dockerfile as existing service)  
**Project Type**: Single Spring Boot service (appointment scheduling extends `com.pm.patient_service`)  
**Performance Goals**: p95 ≤ 500ms for booking (includes overlap check + insert); p95 ≤ 200ms for retrieval  
**Constraints**: Zero double-bookings — overlap detection runs inside a `@Transactional` block with `PESSIMISTIC_WRITE` lock on conflicting appointment rows; appointment duration fixed at 30 minutes (named constant)  
**Scale/Scope**: Single clinic; low-to-moderate concurrent load; no multi-tenancy in this iteration

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| # | Gate | Status |
|---|------|--------|
| 1 | TDD: All new code paths have a failing test written before implementation | ✅ Enforced — task plan puts test tasks before implementation tasks |
| 2 | Clean Code: No methods > 40 lines; all names intention-revealing; no dead code | ✅ Design ensures SRP per class; overlap query in dedicated repository method |
| 3 | Hexagonal: Domain/application layer contains zero Spring/JPA/framework annotations | ✅ `domain/model/Appointment.java` is pure Java; `application/` contains only interfaces and plain service |
| 4 | Dependency rule: No infrastructure import present in domain or application layer | ✅ `AppointmentService` depends only on port interfaces; JPA entity lives under `infrastructure/persistence/` |
| 5 | Constructor injection used throughout application layer; no `@Autowired` fields | ✅ All dependencies wired via constructor; `@Autowired` absent from domain and application layers |
| 6 | Unit tests cover domain + use cases with no Spring context loaded | ✅ `AppointmentTest` and `AppointmentServiceTest` use plain JUnit 5 + Mockito |
| 7 | Adapter/integration tests use `@WebMvcTest` or Testcontainers, not the full context | ✅ Controller tested with `@WebMvcTest`; persistence adapter tested with Testcontainers PostgreSQL |

**All 7 gates pass. Design may proceed.**

## Project Structure

### Documentation (this feature)

```text
specs/1-appointment-scheduling/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── appointments-api.yaml
└── tasks.md             # Phase 2 output (/speckit.tasks — NOT created here)
```

### Source Code (repository root)

```text
src/main/java/com/pm/patient_service/

├── domain/
│   └── model/
│       ├── Appointment.java                    ← pure domain entity (no JPA/Spring)
│       └── AppointmentStatus.java              ← enum: SCHEDULED, CANCELLED

├── application/
│   └── port/
│       ├── in/
│       │   ├── BookAppointmentUseCase.java      ← inbound port
│       │   ├── GetAppointmentUseCase.java       ← inbound port
│       │   ├── CancelAppointmentUseCase.java    ← inbound port
│       │   └── RescheduleAppointmentUseCase.java← inbound port
│       └── out/
│           ├── AppointmentRepositoryPort.java   ← outbound port (persistence)
│           └── PatientValidationPort.java       ← outbound port (patient status)

├── service/
│   └── AppointmentService.java                 ← implements all 4 in-ports; injected via constructor

├── infrastructure/
│   └── persistence/
│       ├── AppointmentJpaEntity.java            ← @Entity; JPA annotations confined here
│       ├── AppointmentJpaRepository.java        ← Spring Data JPA repo (used only by adapter)
│       ├── AppointmentPersistenceAdapter.java   ← implements AppointmentRepositoryPort
│       └── PatientValidationAdapter.java        ← implements PatientValidationPort

├── controller/
│   └── AppointmentController.java              ← REST inbound adapter; delegates to in-ports only

├── dto/
│   ├── AppointmentRequestDTO.java              ← booking request; Jakarta Validation here
│   ├── AppointmentResponseDTO.java             ← booking/retrieval response
│   └── RescheduleRequestDTO.java               ← reschedule request (new start time)

├── mapper/
│   └── AppointmentMapper.java                  ← domain ↔ DTO and domain ↔ JPA entity

└── exception/
    ├── AppointmentNotFoundException.java
    └── AppointmentConflictException.java

─────────────────────────────────────────────────────────────────────────────

src/test/java/com/pm/patient_service/

├── domain/model/
│   └── AppointmentTest.java                    ← unit: domain invariants, status transitions

├── service/
│   └── AppointmentServiceTest.java             ← unit: all 4 use cases (Mockito ports)

├── infrastructure/persistence/
│   └── AppointmentPersistenceAdapterTest.java  ← integration: Testcontainers PostgreSQL

└── controller/
    └── AppointmentControllerTest.java          ← slice: @WebMvcTest, mocked service
```

**Structure Decision**: Single-project layout. Appointment scheduling is a new
hexagonal sub-domain within the existing `com.pm.patient_service` package. The
`domain/model/` and `application/port/out/` packages are net-new; all other
directories extend existing ones. No new modules or build artifacts required.

## Complexity Tracking

> No constitution violations. No complexity justification required.

## Summary

[Extract from feature spec: primary requirement + technical approach from research]

## Technical Context

<!--
  ACTION REQUIRED: Replace the content in this section with the technical details
  for the project. The structure here is presented in advisory capacity to guide
  the iteration process.
-->

**Language/Version**: [e.g., Python 3.11, Swift 5.9, Rust 1.75 or NEEDS CLARIFICATION]  
**Primary Dependencies**: [e.g., FastAPI, UIKit, LLVM or NEEDS CLARIFICATION]  
**Storage**: [if applicable, e.g., PostgreSQL, CoreData, files or N/A]  
**Testing**: [e.g., pytest, XCTest, cargo test or NEEDS CLARIFICATION]  
**Target Platform**: [e.g., Linux server, iOS 15+, WASM or NEEDS CLARIFICATION]
**Project Type**: [single/web/mobile - determines source structure]  
**Performance Goals**: [domain-specific, e.g., 1000 req/s, 10k lines/sec, 60 fps or NEEDS CLARIFICATION]  
**Constraints**: [domain-specific, e.g., <200ms p95, <100MB memory, offline-capable or NEEDS CLARIFICATION]  
**Scale/Scope**: [domain-specific, e.g., 10k users, 1M LOC, 50 screens or NEEDS CLARIFICATION]

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| # | Gate | Status |
|---|------|--------|
| 1 | TDD: All new code paths have a failing test written before implementation | ☐ |
| 2 | Clean Code: No methods > 40 lines; all names intention-revealing; no dead code | ☐ |
| 3 | Hexagonal: Domain/application layer contains zero Spring/JPA/framework annotations | ☐ |
| 4 | Dependency rule: No infrastructure import present in domain or application layer | ☐ |
| 5 | Constructor injection used throughout application layer; no `@Autowired` fields | ☐ |
| 6 | Unit tests cover domain + use cases with no Spring context loaded | ☐ |
| 7 | Adapter/integration tests use `@WebMvcTest` or Testcontainers, not the full context | ☐ |

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)
<!--
  ACTION REQUIRED: Replace the placeholder tree below with the concrete layout
  for this feature. Delete unused options and expand the chosen structure with
  real paths (e.g., apps/admin, packages/something). The delivered plan must
  not include Option labels.
-->

```text
# [REMOVE IF UNUSED] Option 1: Single project (DEFAULT)
src/
├── models/
├── services/
├── cli/
└── lib/

tests/
├── contract/
├── integration/
└── unit/

# [REMOVE IF UNUSED] Option 2: Web application (when "frontend" + "backend" detected)
backend/
├── src/
│   ├── models/
│   ├── services/
│   └── api/
└── tests/

frontend/
├── src/
│   ├── components/
│   ├── pages/
│   └── services/
└── tests/

# [REMOVE IF UNUSED] Option 3: Mobile + API (when "iOS/Android" detected)
api/
└── [same as backend above]

ios/ or android/
└── [platform-specific structure: feature modules, UI flows, platform tests]
```

**Structure Decision**: [Document the selected structure and reference the real
directories captured above]

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |

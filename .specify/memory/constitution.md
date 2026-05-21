<!--
SYNC IMPACT REPORT
==================
Version Change: N/A (initial ratification) → 1.0.0
Modified Principles: N/A — initial constitution
Added Sections:
  - Core Principles I–V
  - Technology Stack & Constraints
  - Development Workflow
  - Governance
Removed Sections: N/A
Templates Updated:
  - .specify/templates/tasks-template.md ✅ (removed OPTIONAL label on tests; TDD is non-negotiable)
  - .specify/templates/plan-template.md ✅ (Constitution Check gates aligned with principles)
  - .specify/templates/spec-template.md — no change required
Follow-up TODOs: None
-->

# Appointment Scheduling and Retrieval Service Constitution

## Core Principles

### I. Test-First Development (NON-NEGOTIABLE)

TDD is mandatory on this project without exception. The Red-Green-Refactor cycle
MUST be strictly followed for every production code change:

- Tests MUST be written before any production implementation code.
- A failing test MUST exist before writing the implementation it covers.
- No production code MUST be introduced without a corresponding failing test.
- All domain logic, use cases, and adapter contracts MUST have unit tests.
- Test coverage for the domain and application layers MUST NOT fall below 80%.

**Rationale**: TDD is the primary quality gate. It forces interface design before
implementation, catches regressions early, and ensures the codebase remains
refactorable without fear.

### II. Clean Code (NON-NEGOTIABLE)

All code MUST adhere to Clean Code principles without compromise:

- Naming MUST be intention-revealing: no abbreviations, no single-letter variables
  outside conventional loop indices (`i`, `j`).
- Functions and methods MUST do one thing (Single Responsibility Principle).
- Functions MUST be small: ideally ≤ 20 lines; MUST NOT exceed 40 lines.
- Magic numbers and strings MUST be replaced with named constants or enums.
- Dead code MUST be deleted, not commented out.
- Comments MUST explain WHY, never WHAT — code MUST be self-documenting.
- No duplication: the DRY principle applies across all layers.

**Rationale**: Clean code is the prerequisite for sustainable velocity. Unclean
code incurs compound interest in the form of bugs and costly rework.

### III. Hexagonal Architecture (NON-NEGOTIABLE)

The service MUST follow the Ports & Adapters (Hexagonal Architecture) pattern:

- The **domain layer** MUST contain only pure Java: entities, value objects, domain
  services, and domain events. No Spring, JPA, or any other framework annotation
  is permitted in this layer.
- The **application layer** MUST contain use cases and port interfaces. Use case
  classes MUST implement inbound ports. Outbound port interfaces (repository
  contracts, external-service contracts) MUST be defined here and implemented in
  the infrastructure layer.
- **Inbound adapters** (REST controllers, message consumers) MUST delegate to
  inbound ports only — no business logic is permitted in adapters.
- **Outbound adapters** (JPA repositories, REST clients, messaging publishers)
  MUST implement the outbound port interfaces defined in the application layer.
- The **dependency rule** MUST be respected at all times: outer layers depend on
  inner layers, never the reverse. Infrastructure MUST NOT be imported into the
  domain or application layer.

**Rationale**: Hexagonal Architecture isolates the domain core from volatile
infrastructure decisions, making it straightforward to test the core in isolation
and to swap adapters without touching business logic.

### IV. Dependency Inversion

All cross-boundary dependencies MUST point inward toward the domain:

- Every outbound port dependency MUST be injected via constructor injection —
  field or setter injection is PROHIBITED.
- Concrete infrastructure classes MUST NEVER be referenced from the application
  or domain layer — only port interfaces are permitted.
- Spring's `@Autowired` MUST NOT appear in domain or application layer classes.

**Rationale**: Dependency inversion enables the application layer to be fully
tested with mocks or stubs without starting any infrastructure, dramatically
reducing test complexity and execution time.

### V. Layered Testing Strategy

Testing MUST be organized across three layers that align with hexagonal boundaries:

- **Unit tests** MUST cover all domain entities, value objects, domain services,
  and application use cases in complete isolation (no Spring context, no DB).
- **Adapter / integration tests** MUST verify that each adapter correctly fulfills
  its port contract (e.g., repository tests with Testcontainers, controller tests
  with `@WebMvcTest`).
- **Acceptance tests** SHOULD cover the primary scheduling and retrieval journeys
  against a fully-assembled Spring context using Testcontainers.
- Tests MUST NOT assert on implementation internals — assertions MUST be based on
  observable outputs and port contracts only.

**Rationale**: Layered testing ensures fast feedback from unit tests, confidence
from integration tests, and regression safety from acceptance tests — without
coupling test code to incidental implementation details.

## Technology Stack & Constraints

- **Language / Runtime**: Java 21 (LTS)
- **Framework**: Spring Boot 3.x — permitted only in adapter and configuration layers
- **Build Tool**: Maven
- **Persistence**: Spring Data JPA with PostgreSQL; schema migrations via Flyway
- **Unit Testing**: JUnit 5 + Mockito
- **Integration / Adapter Testing**: Spring Boot Test + Testcontainers
- **API Style**: RESTful JSON over HTTP; OpenAPI 3 contract published at `/v3/api-docs`
- **Validation**: Jakarta Bean Validation in inbound adapters ONLY — NEVER in the
  domain or application layer
- **No direct database access** from the domain or application layer: ALL persistence
  MUST go through outbound port interfaces.

## Development Workflow

1. **Red**: Write a failing test that expresses the acceptance scenario or unit
   behaviour. Commit with prefix `test:`.
2. **Green**: Write the minimum production code required to make the test pass.
   No extras, no speculative additions.
3. **Refactor**: Improve structure, naming, and clarity without changing observable
   behaviour. All tests MUST remain green after refactoring.
4. **Architecture check**: Before marking a task complete, verify that all changed
   code respects hexagonal layer boundaries and the dependency rule.
5. **PR gate**: Every pull request MUST have all tests passing, no Checkstyle
   violations, and a reviewer sign-off confirming both TDD and architecture
   principles are satisfied.

## Governance

- This constitution supersedes all other development guidelines for this service.
- Any amendment MUST be proposed as a pull request against
  `.specify/memory/constitution.md`, include a written rationale, and be approved
  by at least one maintainer.
- Version MUST be bumped per semantic versioning:
  - **MAJOR**: Principle removed, redefined, or governance scope fundamentally changed.
  - **MINOR**: New principle added or existing principle materially expanded.
  - **PATCH**: Clarification, wording fix, or non-semantic refinement.
- Compliance is reviewed at each sprint retrospective. Recurring violations MUST be
  addressed with a documented remediation plan before the next sprint begins.
- PRs that violate hexagonal architecture boundaries MUST NOT be merged under any
  deadline pressure. Architecture integrity is non-negotiable.

**Version**: 1.0.0 | **Ratified**: 2026-05-21 | **Last Amended**: 2026-05-21

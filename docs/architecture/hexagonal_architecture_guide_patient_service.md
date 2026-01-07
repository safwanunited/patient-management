# 🧩 Hexagonal Architecture Guide – Patient Service (Spring Boot)

## 🎯 Goal

The goal of this guide is to **migrate and enforce Hexagonal Architecture (Ports & Adapters)** in the Patient Service Spring Boot project so that:

- Business logic is framework-agnostic
- Controllers do not contain business logic
- Persistence is isolated behind ports
- Spring and JPA exist only at the edges

This document is **mandatory** for all new APIs and refactoring work.

---

## 🧠 Architectural Principles

### Dependency Rule (Non-Negotiable)

```
Adapters → Ports → Domain
```

Dependencies must always point **inward**.

---

## 📁 Mandatory Package Structure

Create or align your project to the following structure:

```
com.pm.patient_service
│
├── domain
│   └── patient
│       ├── model
│       ├── service
│       └── exception
│
├── application
│   ├── port
│   │   ├── in
│   │   └── out
│   └── service
│
├── adapter
│   ├── in
│   │   └── web
│   └── out
│       └── persistence
│
└── config
```

This structure must be created **before** implementing new features.

---

## 1️⃣ Domain Layer (Inside the Hexagon)

### Rules
- No Spring annotations
- No JPA annotations
- No DTOs
- Pure Java only

### Allowed
- Domain models
- Domain services
- Business rules

### Not Allowed
- `@Entity`, `@Service`, `@Repository`
- Spring utilities

---

## 2️⃣ Inbound Ports (Use Cases)

### Rules
- Interfaces only
- One interface per use case
- Represents application behavior

### Example
```java
public interface CreatePatientUseCase {
    PatientResponseDTO create(PatientRequestDTO request);
}
```

Controllers must depend **only** on inbound ports.

---

## 3️⃣ Outbound Ports (Dependencies)

### Rules
- Interfaces only
- Represents what the application needs

### Example
```java
public interface PatientRepositoryPort {
    Patient save(Patient patient);
    boolean existsByEmail(String email);
}
```

Application services depend on outbound ports, not implementations.

---

## 4️⃣ Application Services

### Rules
- Implements inbound ports
- Uses outbound ports
- No Spring annotations

### Responsibility
- Coordinate domain logic
- Enforce business rules

---

## 5️⃣ Adapters (Outside the Hexagon)

### Inbound Adapters (Web)
- REST Controllers
- Validation
- HTTP mapping

### Outbound Adapters (Persistence)
- JPA entities
- Spring Data repositories
- External system clients

### Rules
- Adapters implement ports
- No adapter bypasses ports

---

## 6️⃣ Configuration Layer

### Rules
- Spring wiring only
- No business logic

### Example
```java
@Configuration
public class PatientConfig {

    @Bean
    CreatePatientUseCase createPatientUseCase(PatientRepositoryPort port) {
        return new PatientService(port);
    }
}
```

---

## 🧪 Testing Strategy

| Layer | Test Type |
|-----|---------|
| Domain | Unit tests (no Spring) |
| Application | Unit tests with mocks |
| Adapters | Integration tests |

---

## 🧭 Development Workflow (MANDATORY)

### Step 1 – Create Feature Docs

```
/docs/features/<STORY-ID>/
```

Add:
- `<STORY-ID>_domain_model.md`
- `<STORY-ID>_ports.md`
- `<STORY-ID>_adapters.md`

---

### Step 2 – Prime GitHub Copilot

Paste this comment when starting work:

```java
/*
Architecture Context:
- Hexagonal Architecture
- Domain is pure Java
- Ports are interfaces
- Controllers depend on inbound ports only
- Persistence via outbound ports

Task:
Implement <STORY-ID>
Start with domain and ports only.
*/
```

---

### Step 3 – Coding Order (Do Not Change)

1. Domain
2. Inbound ports
3. Outbound ports
4. Application service
5. Adapters
6. Configuration

---

## 🚨 Definition of Done

Before merging:
- [ ] No Spring/JPA in domain
- [ ] Controllers depend only on inbound ports
- [ ] Persistence accessed via outbound ports
- [ ] Spring wiring isolated in config
- [ ] Feature docs updated

---

## 💡 Guiding Principle

> If Spring is removed, the domain must still compile.

Follow the hexagon 🧩


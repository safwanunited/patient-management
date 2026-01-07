Create a file ‘hexagonal-architecture.instructions.md’ inside the Copilot Instructions folder in your local repository, and copy the below content.
[Trial]
# AI Assistant Guide: Hexagonal Architecture
> **Purpose**: Language-agnostic quick reference for AI coding assistants to enforce hexagonal architecture patterns
## 🎯 Core Rules (Must Follow)
### 1. Dependency Direction
```
Outer Layers → Inner Layers
Adapters → Application → Domain
NEVER: Inner → Outer
```
### 2. Layer Responsibilities
**Domain Layer** (`domain/`)
- ✅ Business logic, entities, value objects, domain services
- ❌ NO framework imports, NO database annotations, NO external dependencies
  **Application Layer** (`application/`)
- ✅ Use cases, port interfaces (input/output), DTOs
- ✅ Depends on: Domain only
- ❌ NO concrete adapter implementations
  **Adapters Layer** (`adapters/`)
- **Primary** (`adapters/primary/`): REST, GraphQL, CLI
- ✅ Depends on: Input ports
- ✅ Handles: HTTP, presentation logic
- **Secondary** (`adapters/secondary/`): Database, external APIs
- ✅ Implements: Output ports
- ✅ Handles: Persistence, external communication
  **Infrastructure** (`infrastructure/`)
- ✅ Dependency injection, configuration, bootstrapping
## 🚫 Forbidden Patterns (Auto-Reject)
```
// ❌ Domain importing adapters/frameworks
domain/User imports ORM_Framework
domain/Service imports HTTP_Framework
domain/ValueObject imports HTTP_Client
// ❌ Domain with framework decorators/annotations
@DatabaseEntity     // in domain/entities/
@JsonSerializable  // in domain/entities/
class User { }
// ❌ Use case with concrete adapter
class CreateUser {
 constructor(repository: PostgresUserRepository)  // Use interface!
}
// ❌ Adapter to adapter
class Controller {
 constructor(repository: DatabaseRepository)  // Use port interface!
}
// ❌ Business logic in adapters
class UserController {
 method create() {
   if (email.contains('@')) { /* validation */ }  // Move to domain!
 }
}
```
## ✅ Correct Patterns (Always Use)
### Pattern 1: Domain Entity
```
// domain/entities/User
class User {
 - id: String (readonly)
 - email: String (private)
  constructor(id, email)
  method changeEmail(newEmail):
   if not newEmail.contains('@'):
     throw DomainError('Invalid email')
   this.email = newEmail
  // Pure methods, no framework dependencies
 // No annotations/decorators
 // No database/HTTP concerns
}
```
### Pattern 2: Port Definitions
```
// application/ports/input/IUserService
interface IUserService {
 method createUser(dto: CreateUserDTO): UserDTO
 method getUserById(id: String): UserDTO
}
// application/ports/output/IUserRepository
interface IUserRepository {
 method save(user: User): void
 method findById(id: String): User or null
}
```
### Pattern 3: Use Case
```
// application/useCases/CreateUserUseCase
class CreateUserUseCase implements IUserService {
 - userRepo: IUserRepository  // Interface dependency
  constructor(userRepo: IUserRepository)
  method createUser(dto: CreateUserDTO): UserDTO {
   user = new User(generateId(), dto.email)
   userRepo.save(user)
   return toDTO(user)
 }
}
```
### Pattern 4: Primary Adapter
```
// adapters/primary/rest/UserController
class UserController {
 - userService: IUserService  // Port dependency
  constructor(userService: IUserService)
  method handleCreateUser(request, response) {
   result = userService.createUser(request.body)
   response.json(result)
 }
}
```
### Pattern 5: Secondary Adapter
```
// adapters/secondary/persistence/PostgresUserRepository
class PostgresUserRepository implements IUserRepository {
 - db: Database
  constructor(db: Database)
  method save(user: User): void {
   db.execute(
     'INSERT INTO users VALUES (?, ?)',
     [user.getId(), user.getEmail()]
   )
 }
  method findById(id: String): User or null {
   row = db.queryOne('SELECT * FROM users WHERE id = ?', [id])
   if not row: return null
   return new User(row.id, row.email)
 }
}
```
## 📁 File Placement Rules
```
src/
├── domain/
│   ├── entities/           → User
│   ├── valueObjects/       → Email
│   ├── services/           → UserValidator
│   └── exceptions/         → DomainError
│
├── application/
│   ├── useCases/          → CreateUserUseCase
│   ├── ports/
│   │   ├── input/         → IUserService
│   │   └── output/        → IUserRepository
│   └── dto/               → CreateUserDTO
│
├── adapters/
│   ├── primary/
│   │   ├── rest/          → UserController
│   │   ├── graphql/       → UserResolver
│   │   └── cli/           → UserCommand
│   └── secondary/
│       ├── persistence/   → PostgresUserRepository
│       ├── messaging/     → RabbitMQPublisher
│       └── external/      → EmailServiceClient
│
└── infrastructure/
   ├── di/                → container
   └── config/            → database.config
```
## 🔍 Code Review Checklist
When generating/reviewing code, verify:
- [ ] Domain files import ONLY domain files or standard library
- [ ] No ORM/database annotations in domain layer
- [ ] No HTTP/JSON/XML annotations in domain layer
- [ ] Use cases depend on interfaces, not concrete classes
- [ ] All repositories/services injected via interfaces
- [ ] Business logic in domain/application, not adapters
- [ ] DTOs used for crossing boundaries
- [ ] No adapter → adapter dependencies
- [ ] Dependencies flow: Adapters → Application → Domain
## 🎨 Quick Decision Tree
**When adding code, ask:**
1. **Is it business logic/rules?** → Domain layer
2. **Is it a use case/workflow?** → Application layer
3. **Is it HTTP/REST/GraphQL related?** → Primary adapter
4. **Is it database/external API/messaging?** → Secondary adapter
5. **Is it configuration/wiring?** → Infrastructure
   **When importing/using:**
- Domain can import: Domain only + standard library
- Application can import: Domain + Application
- Adapters can import: Domain + Application + specific Adapter + frameworks
- Infrastructure can import: Everything
## 🚀 Common Tasks
### Add New Entity
1. Create in `domain/entities/`
2. No framework annotations/decorators
3. Pure business methods only
4. No references to databases, HTTP, or external systems
### Add New Use Case
1. Create in `application/useCases/`
2. Define input port in `application/ports/input/`
3. Define output ports in `application/ports/output/`
4. Inject ports via constructor (dependency injection)
### Add REST Endpoint
1. Create controller in `adapters/primary/rest/`
2. Inject input port (use case interface)
3. Handle HTTP concerns only (routing, status codes, headers)
4. Convert requests to DTOs
### Add Database Support
1. Create repository in `adapters/secondary/persistence/`
2. Implement output port interface
3. Map between domain entities and DB models/documents
4. Handle all ORM/database-specific code here
### Add External API Integration
1. Create client in `adapters/secondary/external/`
2. Implement output port interface
3. Handle HTTP clients, retries, circuit breakers
4. Map external responses to domain objects
## ⚡ Quick Fixes for Common Violations
**Found: Database annotation in domain**
```
// ❌ Wrong
@DatabaseEntity
class User { }
// ✅ Fix: Remove annotation, add mapping in adapter
class User { }  // in domain/
class UserEntity { @DatabaseEntity }  // in adapters/secondary/persistence/
```
**Found: Use case with concrete dependency**
```
// ❌ Wrong
constructor(repository: PostgresRepo)
// ✅ Fix: Use interface
constructor(repository: IUserRepository)
```
**Found: Business logic in controller**
```
// ❌ Wrong (in controller)
if email.contains('@'):
 // validation
// ✅ Fix: Move to domain
class User:
 method changeEmail(email):
   if not email.contains('@'):
     throw DomainError()
```
**Found: Framework import in domain**
```
// ❌ Wrong
import HTTPClient  // in domain/
import ORMFramework  // in domain/
// ✅ Fix: Remove imports, use ports
// Define port in application/, implement in adapter
```
## 📝 Naming Conventions
- **Input Ports**: `{Feature}Service`, `{Action}UseCase`, `{Feature}InputPort`
- **Output Ports**: `{Resource}Repository`, `{External}Service`, `{Resource}OutputPort`
- **Use Cases**: `{Action}{Resource}UseCase`, `{Action}{Resource}Service`
- **Controllers**: `{Resource}Controller`, `{Resource}Handler`
- **Repositories**: `{Technology}{Resource}Repository`, `{Database}{Resource}Repository`
- **DTOs**: `{Action}{Resource}Request`, `{Resource}Response`
## 🎯 Remember
1. **Domain is sacred**: No external dependencies, ever (only standard library)
2. **Depend on abstractions**: Use interfaces/contracts for dependencies
3. **One direction**: Dependencies always point inward
4. **Separation**: Adapters talk through ports, never directly
5. **Business in domain**: Keep logic where it belongs
## 🛠️ Technology Mapping
### Common Framework Violations to Avoid:
**Java/Spring:**
- ❌ `@Entity`, `@Table`, `@Column` in domain
- ❌ `@RestController`, `@RequestMapping` in domain/application
- ✅ Use plain POJOs in domain
  **C#/.NET:**
- ❌ `[Table]`, `[Column]`, `[Key]` in domain
- ❌ `[ApiController]`, `[Route]` in domain/application
- ✅ Use plain classes in domain
  **Python:**
- ❌ SQLAlchemy declarative base in domain
- ❌ Flask/Django decorators in domain
- ✅ Use plain classes/dataclasses in domain
  **Node.js/TypeScript:**
- ❌ TypeORM/Mongoose decorators in domain
- ❌ Express decorators in domain
- ✅ Use plain classes in domain
  **Go:**
- ❌ Struct tags for JSON/DB in domain
- ❌ HTTP handlers in domain
- ✅ Use plain structs in domain
  **Ruby:**
- ❌ ActiveRecord inheritance in domain
- ❌ Rails-specific code in domain
- ✅ Use plain Ruby objects in domain
## 📊 Architecture Validation
### Critical Violations (Must Fix Immediately):
1. Framework imports in domain layer
2. Database/ORM annotations in domain entities
3. HTTP/presentation concerns in domain
4. Concrete adapter dependencies in use cases
5. Business logic in adapters
6. Circular dependencies between layers
7. Adapter-to-adapter direct dependencies
### Warning Signs (Review Carefully):
1. Large use cases (consider splitting)
2. Anemic domain models (move logic to domain)
3. DTOs with business logic (move to domain)
4. Ports with too many methods (consider splitting)
5. Domain services with external dependencies
---
*Use this guide to enforce hexagonal architecture in all code generation and reviews, regardless of programming language.*
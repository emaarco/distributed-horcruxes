# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This repository demonstrates solutions to distributed transaction problems when working with Spring Boot and Zeebe (Camunda 8). It provides practical examples of patterns that ensure consistency between database transactions and process engine interactions.

**Core Problem**: When a service needs to coordinate database operations with Zeebe process engine operations, failures can cause inconsistent states. The examples show different approaches to handle this coordination.

## Commands

### Infrastructure
```bash
# Start required infrastructure (Zeebe, Operate, PostgreSQL, Elasticsearch)
cd stack && docker-compose up

# Access Operate UI at http://localhost:9081 (credentials: demo/demo)
```

### Build & Run
```bash
# Build all modules
gradle build

# Build specific example
gradle :examples:base-scenario:build
gradle :examples:after-transaction:build
gradle :examples:outbox-pattern:build

# Run tests
gradle test

# Clean build artifacts
gradle clean
```

### Running Examples
Each example is a Spring Boot application. Run the main class:
- Base Scenario: `examples/base-scenario/src/main/kotlin/de/emaarco/example/ExampleApplication.kt` (port 8082)
- After-Transaction: `examples/after-transaction/src/main/kotlin/de/emaarco/example/ExampleApplication.kt` (port 8081)
- Outbox Pattern: `examples/outbox-pattern/src/main/kotlin/de/emaarco/example/ExampleApplication.kt`

All examples connect to:
- Zeebe gRPC: localhost:26500
- Zeebe REST: localhost:9600
- PostgreSQL: localhost:5432 (database: example_database, credentials: admin/admin)

### Interacting with Examples
Use Bruno API client with files in `bruno/` directory:
- `subscribe-to-newsletter.bru` - Subscribe to newsletter (creates subscription and starts process)
- `confirm-subscription.bru` - Confirm subscription (triggers process continuation)

### BPMN Model Generation
```bash
# Generate Kotlin models from BPMN files
gradle generateBpmnModels

# BPMN source: configuration/newsletter.bpmn
# Generated output: examples/*/src/main/kotlin/de/emaarco/example/adapter/process/
```

## Architecture

### High-Level Structure
The examples follow **hexagonal architecture** (ports and adapters):

```
adapter/
  in/
    rest/          - REST controllers for user requests
    zeebe/         - Zeebe job workers that handle process tasks
  out/
    db/            - JPA repositories and persistence adapters
    zeebe/         - Process engine adapters that send messages/start processes
application/
  service/         - Business logic with @Transactional boundaries
  port/
    out/           - Port interfaces for repositories and process engine
domain/            - Domain entities and value objects
```

**Key architectural point**: Transactions are defined at the service layer. Services coordinate database operations and process engine interactions through port interfaces, which is where the distributed transaction challenge occurs.

### Pattern Implementations

#### Base Scenario (`examples/base-scenario`)
- **Purpose**: Demonstrates the distributed transaction problem WITHOUT any solution
- **Core class**: `ProcessEngineApi` in `adapter/out/zeebe/` - calls Zeebe directly
- **Mechanism**: Naive implementation that calls Zeebe immediately within the transaction
- **Problems demonstrated**:
  - Premature execution (process starts before DB commit)
  - Out-of-sync states (DB fails after notifying Zeebe)
  - Race conditions (workers see uncommitted data)
- **Trade-off**: Simple but broken - DO NOT use in production
- **Use case**: Understanding what goes wrong and why solutions are needed

#### After-Transaction Pattern (`examples/after-transaction`)
- **Core class**: `ProcessTransactionManager` in `adapter/out/zeebe/`
- **Mechanism**: Uses Spring's `TransactionSynchronizationManager` to register callbacks that execute after database commit
- **Pre-commit check**: Validates Zeebe broker health before committing
- **Post-commit**: Sends message/starts process only after database transaction succeeds
- **Trade-off**: Fast but no retry logic if post-commit fails

#### Outbox Pattern (`examples/outbox-pattern`)
- **Core classes**:
  - `ProcessMessageEntity` - Outbox table entity in `adapter/out/db/message/`
  - `ProcessEngineOutboxScheduler` - Polls and sends messages
- **Mechanism**: Saves messages to database table in same transaction, scheduler processes them periodically
- **Retry**: Scheduler retries failed messages on next run (every 10 seconds)
- **Trade-off**: Reliable with retries but introduces latency

### Shared Domain Model
All examples use the same newsletter subscription process:
1. User submits subscription form → Process starts → Sends confirmation email
2. User confirms subscription → Process continues → Sends welcome email
3. Process completes

The BPMN model (`configuration/newsletter.bpmn`) is shared across both examples and deployed at application startup via `@Deployment` annotation.

## Important Context

### Zeebe Integration
- Uses `spring-zeebe` client library (Camunda 8)
- Process definitions are in `configuration/` and shared across examples
- Workers use `@JobWorker` annotation to handle Zeebe job types
- Messages are sent via `ZeebeClient` API (wrapped in adapter implementations)

### Database Configuration
- PostgreSQL with Hibernate JPA
- `ddl-auto: create` - Database schema recreated on each startup (development only)
- Each example has its own database schema but connects to same database

### Testing
There are currently no automated tests in this repository. When implementing features, focus on manual testing via the Bruno API files and monitoring in Operate.

### Distributed Transaction Challenges
See `challanges.md` for detailed explanation of the six main problems:
1. Premature execution (engine starts before DB commit)
2. Out-of-sync states (DB fails after notifying engine)
3. Conflicting data (tasks execute out of order)
4. Duplicate calls (retries create duplicates)
5. Network issues (job complete fails to reach engine)
6. Task no longer available (engine cancelled task before worker completed)

Each pattern addresses these challenges with different trade-offs in terms of speed, reliability, and complexity.
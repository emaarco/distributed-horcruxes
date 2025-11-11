# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This repository demonstrates solutions to distributed transaction problems when working with Spring Boot and Zeebe (
Camunda 8). It provides practical examples of patterns that ensure consistency between database transactions and process
engine interactions.

**Core Problem**: When a service needs to coordinate database operations with Zeebe process engine operations, failures
can cause inconsistent states. The examples show different approaches to handle this coordination.

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
gradle :examples:<pattern-name>:build
# Available: base-scenario, after-transaction, outbox-pattern, idempotency-pattern

# Run tests
gradle test

# Clean build artifacts
gradle clean
```

### Running Examples

Each example is a Spring Boot application on port 8081 (except base-scenario on 8082).

Run the main class:

- `examples/<pattern-name>/src/main/kotlin/de/emaarco/example/ExampleApplication.kt`

Available patterns: `base-scenario`, `after-transaction`, `outbox-pattern`, `idempotency-pattern`

All connect to:

- Zeebe gRPC: localhost:26500
- Zeebe REST: localhost:9600
- PostgreSQL: localhost:5432 (database: example_database, user: admin/admin)

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

### Creating GitHub Issues

When creating issues for this repository, follow these guidelines:

```bash
# Create issues using gh CLI
gh issue create --title "..." --body "..."
```

**Principles**:

- **KISS** (Keep It Simple, Stupid) - Keep issues compact and focused
- **User Story Format** - Titles should be user stories: "As a [role], I want [feature] so that [benefit]"
- **Context over Checklists** - Explain **what** the problem is and **why** it matters, not how to implement it
- **Solution-Concept** - Give developers an idea of how to approach the problem
- **References** - Include links to relevant docs, examples, or related code

**Example**:

```
Title: As a developer, I want [feature] to [achieve goal]

Body:
## User Story
As a [role], I want [feature], so that [benefit].

## Problem
[What's wrong? Why does it matter?]

## Solution Concept
[High-level idea of how to solve it]

## References
- Related code/docs
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

**Key point**: Use `@Transactional` at service layer. Idempotency checks happen there too.

### Pattern Implementations

| Pattern                 | Problem Solved           | How It Works                    | Trade-off                     |
|-------------------------|--------------------------|---------------------------------|-------------------------------|
| **Base Scenario**       | None (shows the problem) | Calls Zeebe during transaction  | ❌ Broken - don't use          |
| **After-Transaction**   | Premature execution      | Callbacks after DB commit       | ✅ Fast, ❌ No retry            |
| **Outbox Pattern**      | Transaction coordination | DB table + background scheduler | ✅ Reliable + retry, ❌ Latency |
| **Idempotency Pattern** | Duplicate executions     | Track completed operations      | ✅ Prevents duplicates         |

#### Base Scenario (`examples/base-scenario`)

Shows what goes wrong: process starts before DB commits, causing race conditions and inconsistent state.

#### After-Transaction (`examples/after-transaction`)

Uses Spring's `TransactionSynchronizationManager` to send Zeebe messages only after DB commits successfully.

#### Outbox Pattern (`examples/outbox-pattern`)

Saves messages to DB table in same transaction. Background scheduler sends them to Zeebe with retries.

#### Idempotency Pattern (`examples/idempotency-pattern`)

Services check `processed_operations` table before executing. Uses composite key: `subscriptionId-elementId`.
**Pattern**: Check if processed → Execute → Record completion (all in one transaction).

### Shared Domain Model

All examples use the same newsletter subscription process:

1. User submits subscription form → Process starts → Sends confirmation email
2. User confirms subscription → Process continues → Sends welcome email
3. Process completes

The BPMN model (`configuration/newsletter.bpmn`) is shared across both examples and deployed at application startup via
`@Deployment` annotation.

## Important Context

### Zeebe Integration

- Uses `spring-zeebe` client library (Camunda 8)
- Process definitions are in `configuration/` and shared across examples
- Workers use `@JobWorker` annotation to handle Zeebe job types
- Messages are sent via `CamundaClient` API (wrapped in adapter implementations)

### Database Configuration

- PostgreSQL with Hibernate JPA
- `ddl-auto: create` - Database schema recreated on each startup (development only)
- Each example has its own database schema but connects to same database

### Testing

There are currently no automated tests in this repository. When implementing features, focus on manual testing via the
Bruno API files and monitoring in Operate.

### Distributed Transaction Challenges

Six main problems (see `challenges.md` for details):

1. Premature execution - Process starts before DB commits
2. Out-of-sync states - DB fails after notifying Zeebe
3. Conflicting data - Tasks execute out of order
4. Duplicate calls - Retries create duplicates
5. Network issues - Job completion lost
6. Task unavailable - Worker completes cancelled task

**Which pattern solves what?**

- After-Transaction: #1, #2
- Outbox Pattern: #1, #2, #3 (with retries)
- Idempotency Pattern: #4
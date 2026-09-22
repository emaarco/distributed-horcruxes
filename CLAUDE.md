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

# Access Operate UI at http://localhost:8080/operate (credentials: demo/demo)
# (Camunda 8.9 consolidated orchestration cluster serves Operate on port 8080)
```

### Build & Run

```bash
# Build all modules
gradle build

# Build specific example
gradle :examples:<pattern-name>:build
# Available: base-scenario, after-transaction, outbox-pattern, idempotency-pattern, combined-pattern, saga-pattern

# Run tests
gradle test

# Clean build artifacts
gradle clean
```

### Running Examples

Each example is a Spring Boot application on port 8081 (except saga-pattern on 8083).

Run the main class:

- `examples/<pattern-name>/src/main/kotlin/io/miragon/example/ExampleApplication.kt`

Available patterns: `base-scenario`, `after-transaction`, `outbox-pattern`, `idempotency-pattern`

All connect to:

- Zeebe gRPC: localhost:26500
- Zeebe REST: localhost:8080
- PostgreSQL: localhost:5432 (database: example_database, user: admin/admin)

### Interacting with Examples

The `bruno/` directory is an end-to-end scenario suite (Bruno CLI pinned to 4.0.0, poll helpers in
`bruno/collection.bru`, environments `local` for port 8081 and `saga` for port 8083):

- `01-happy-path`, `02-reject-confirmation`, `03-no-empty-spots` — tagged `pattern`, run against any port-8081 example
- `04-saga-compensation` — tagged `saga`, run against saga-pattern

```bash
cd bruno && npx --yes @usebruno/cli@4.0.0 run . --env local --tags pattern -r
cd bruno && npx --yes @usebruno/cli@4.0.0 run . --env saga --tags saga -r
```

Assertions go through the Camunda v2 REST API (`process-instances/search`, `element-instances/search`). The pre-merge
workflow runs the suite as a matrix over after-transaction, outbox-pattern, idempotency-pattern, combined-pattern and
saga-pattern, each with a fresh stack. Locally the spot capacity is in-memory (restart the app between runs) and both
process definitions share the start message, so reset the stack (`docker-compose down -v`) when switching between
saga-pattern and the other examples.

### BPMN Model Generation

```bash
# Generate Kotlin models from BPMN files
gradle generateBpmnModelApi

# BPMN source: examples/<pattern-name>/src/main/resources/inner-circle-membership.bpmn
#              (saga-pattern: inner-circle-membership-compensation.bpmn)
# Generated output: examples/*/src/main/kotlin/io/miragon/example/adapter/process/
# Lint all models: npm run lint:bpmn
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

**Key point**: Use `@Transactional` at service layer. Idempotency is handled there too, via a central
`IdempotentOperationExecutor` that services wrap their business logic in.

### Pattern Implementations

| Pattern                 | Problem Solved           | How It Works                    | Trade-off                     |
|-------------------------|--------------------------|---------------------------------|-------------------------------|
| **Base Scenario**       | None (shows the problem) | Calls Zeebe during transaction  | ❌ Broken - don't use          |
| **After-Transaction**   | Premature execution      | Callbacks after DB commit       | ✅ Fast, ❌ No retry            |
| **Outbox Pattern**      | Transaction coordination | DB table + background scheduler | ✅ Reliable + retry, ❌ Latency |
| **Idempotency Pattern** | Duplicate executions     | Track completed operations      | ✅ Prevents duplicates         |
| **Combined Pattern**    | All of the above         | Outbox + Idempotency together   | ✅ End-to-end safe, ❌ More parts |

#### Base Scenario (`examples/base-scenario`)

Shows what goes wrong: process starts before DB commits, causing race conditions and inconsistent state.

#### After-Transaction (`examples/after-transaction`)

Uses Spring's `TransactionSynchronizationManager` to send Zeebe messages only after DB commits successfully.

#### Outbox Pattern (`examples/outbox-pattern`)

Saves messages to DB table in same transaction. Background scheduler sends them to Zeebe with retries.

#### Idempotency Pattern (`examples/idempotency-pattern`)

Services wrap their business logic in a central `IdempotentOperationExecutor` (`runOnce`), which checks the
`processed_operations` table before executing. Uses composite key: `membershipId-elementId`.
**Pattern**: Check if processed → Execute → Record completion (all in one transaction).

#### Combined Pattern (`examples/combined-pattern`)

Merges both: outbound messages go through the outbox (`process_message` table + scheduler with `messageId` dedup),
inbound jobs are made idempotent via `processed_operations`. Outbox secures service → Zeebe, idempotency secures
Zeebe → service — addresses all six challenges.

### Shared Domain Model

All examples use the MiraVelo "Inner Circle" membership process (a limited, exclusive newsletter; reference model:
`Miragon/miravelo-reference`, membership-program stage 07/08):

1. User submits registration form → Process starts → "Claim membership" reserves one of the limited spots
   (returns `hasEmptySpots`; no spot → rejection mail → end)
2. Confirmation subprocess → Sends confirmation email → waits for `miravelo.membershipConfirmed`
   (reminder timer re-sends the mail; deadline timer or `miravelo.confirmationRejected` declines the membership)
3. User confirms → Welcome mail → Process completes

Each module holds its own copy of the model in `src/main/resources` and deploys it at startup via `@Deployment`.
The five pattern modules use `inner-circle-membership.bpmn` and deliberately never release a declined member's spot.
The saga-pattern uses `inner-circle-membership-compensation.bpmn`, where "Revoke claim" is a compensation handler
attached to "Claim membership" and both decline end events throw compensation.

Job types: `membership.claimMembership`, `membership.sendConfirmationMail`, `membership.sendWelcomeMail`,
`membership.sendRejectionMail`, saga only: `membership.revokeClaim`.
Messages: `miravelo.registrationSubmitted` (start), `miravelo.membershipConfirmed`, `miravelo.confirmationRejected`
(both correlated on `membershipId`).

## Important Context

### Zeebe Integration

- Uses `spring-zeebe` client library (Camunda 8)
- Each example keeps its process definition in its own `src/main/resources`
- Workers use `@JobWorker` annotation to handle Zeebe job types
- Messages are sent via `CamundaClient` API (wrapped in adapter implementations)

### Database Configuration

- PostgreSQL with Hibernate JPA
- `ddl-auto: create` - Database schema recreated on each startup (development only)
- Each example has its own database schema but connects to same database

### Testing

The example modules have unit tests for their application services (`gradle test`). End-to-end behavior is covered by
the Bruno scenario suite (see "Interacting with Examples"), which also runs in CI; use Operate for manual inspection.

### Distributed Transaction Challenges

Six main problems (see `CHALLENGES.md` for details):

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
- Combined Pattern: #1–#6 (Outbox for #1–#3/#5/#6 + Idempotency for #4)

### Productivity Tips:

- When working with GitHub, you can use the gh cli tool
- Use context7 for code generation & general questions whenever working with specific frameworks or libraries.

### GitHub

- Keep commit messages and body short and descriptive.
- When writing pull requests or issues, also write compact. Focus on what changed and why, not on technical details and
  adding bloat like author notes and co.
- When writing issues, always write a summary, the current state, and the desired state. moreover, give a high-level
  overview of the technical details - and whether stuff is breaking or not. Each of these is a compact subchapter. Focus
  on behavior, not implementation. Avoid specific file names, line numbers, or code paths.
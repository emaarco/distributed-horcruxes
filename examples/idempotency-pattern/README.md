# 🔁 Idempotency Pattern Example

This example demonstrates how to handle **duplicate job worker executions** using an idempotency pattern. Zeebe uses at-least-once delivery semantics, meaning job workers may be invoked multiple times for the same job. This pattern tracks completed operations in a database table to prevent duplicate processing.

By checking if an operation has already been completed before executing business logic, this approach ensures that retried jobs don't cause duplicate side effects like sending emails multiple times or double-processing transactions.

## **Overview** 🛠️

The idempotency pattern consists of three main components:

1. **OperationId Value Object**: A composite key combining `membershipId-elementId` for business-driven idempotency tracking
2. **ProcessedOperations Table**: Database table that records completed operations with their operationId and timestamp
3. **IdempotentOperationExecutor**: A central component that performs the Check → Execute → Record cycle; services wrap their business logic in it

**Key Features:**

- **Composite OperationId**: Uses `membershipId-elementId` instead of internal job keys for meaningful tracking
- **Centralized Check**: The Check → Execute → Record logic lives in one reusable executor, not in every service
- **Service-Layer Implementation**: Idempotency logic lives in the application layer, not in workers (clean separation of concerns)
- **Atomic Pattern**: Check → Execute → Record happens in single `@Transactional` boundary
- **Minimal Infrastructure**: Just a simple database table, no schedulers or background processes needed

> **📘 Please note:** This pattern addresses **duplicate job executions** (problem #4 from distributed transaction challenges). It does NOT solve transaction coordination issues between your database and Zeebe. Combine this with After-Transaction or Outbox patterns for complete transaction safety.

## **Code Example** 💻

### **OperationId Value Object**

The `OperationId` wraps a composite key for type-safe operation tracking:

```kotlin
package io.miragon.example.domain

data class OperationId(val value: String)
```

### **ProcessedOperation Entity**

The database entity tracks completed operations:

```kotlin
@Entity
@Table(name = "processed_operations")
data class ProcessedOperationEntity(
    @Id
    @Column(name = "operation_id", nullable = false)
    val operationId: String,

    @Column(name = "processed_at", nullable = false)
    val processedAt: Instant = Instant.now()
)
```

### **The IdempotentOperationExecutor**

The Check → Execute → Record pattern is implemented once, in a reusable executor:

```kotlin
@Component
class IdempotentOperationExecutor(
    private val processedOperationRepository: ProcessedOperationRepository
) {

    private val log = KotlinLogging.logger {}

    fun runOnce(operationId: OperationId, block: () -> Unit) {
        if (processedOperationRepository.existsById(operationId)) {
            log.info { "Skipping already processed operation: ${operationId.value}" }
            return
        }

        block()
        processedOperationRepository.save(operationId)
    }
}
```

**Pattern Breakdown:**
1. **Check**: Query `processed_operations` table for the operationId
2. **Execute**: If not found, run the business logic (the `block`)
3. **Record**: Save the operationId to mark completion

The executor must be called **inside** the caller's transaction: because it runs within the
service's `@Transactional` boundary, all three steps stay atomic, preventing race conditions.

### **Service Layer Implementation**

Services wrap their business logic in `runOnce` and stay focused on business behavior:

```kotlin
@Service
@Transactional
class SendConfirmationMailService(
    private val repository: MembershipRepository,
    private val idempotencyGuard: IdempotentOperationExecutor
) : SendConfirmationMailUseCase {

    private val log = KotlinLogging.logger {}

    override fun sendConfirmationMail(membershipId: MembershipId, operationId: OperationId) {
        idempotencyGuard.runOnce(operationId) {
            val membership = repository.find(membershipId)
            log.info { "Sending confirmation mail to ${membership.email.value}" }
        }
    }
}
```

### **Worker Layer**

Workers construct the composite operationId and delegate to services:

```kotlin
@Component
class SendConfirmationMailWorker(
    private val useCase: SendConfirmationMailUseCase
) {
    private val log = KotlinLogging.logger {}

    @JobWorker(type = ServiceTasks.MEMBERSHIP_SEND_CONFIRMATION_MAIL)
    fun sendConfirmationMail(
        job: ActivatedJob,
        @Variable("membershipId") membershipId: String
    ) {
        log.debug { "Received Zeebe job to send confirmation mail: $membershipId" }
        useCase.sendConfirmationMail(
            MembershipId(UUID.fromString(membershipId)),
            OperationId("$membershipId-${job.elementId}")
        )
    }
}
```

**Composite OperationId Construction:**
- Format: `membershipId-elementId`
- Example: `550e8400-e29b-41d4-a716-446655440000-serviceTask_claimMembership`
- Business-driven: Tied to domain entity (membership) and BPMN element, not internal Zeebe job keys

### **Spot Capacity: Solving the Idempotency Problem** 🎟️

The inner circle has a fixed number of spots. `ClaimMembershipService` reserves one per registration and shows how the
idempotency pattern solves the duplicate processing problem from the [base-scenario](../base-scenario/README.md).

**The Service Implementation:**

```kotlin
@Service
@Transactional
class ClaimMembershipService(
    private val repository: MembershipRepository,
    private val capacity: MembershipCapacity,
    private val idempotencyGuard: IdempotentOperationExecutor
) : ClaimMembershipUseCase {

    override fun claim(membershipId: MembershipId, operationId: OperationId): Boolean {
        idempotencyGuard.runOnce(operationId) {
            val membership = repository.find(membershipId)
            val hasEmptySpots = capacity.reserveSpot()
            val claimedMembership = if (hasEmptySpots) membership.claim() else membership.reject()
            repository.save(claimedMembership)
        }
        return repository.find(membershipId).status == MembershipStatus.CLAIMED
    }
}
```

**How It Works:**

1. Worker handles the `membership.claimMembership` job
2. Constructs `OperationId` from `membershipId-serviceTask_claimMembership`
3. The executor checks if this exact operation was already processed
4. If already processed: skip the reservation and answer from the stored membership status
5. If not processed: reserve a spot, update the membership AND record the operation (atomic)

**The Result:**

Unlike the base-scenario where a retried job reserves a second spot for the same person, here a spot is reserved
**exactly once** per registration, regardless of how many times Zeebe retries the job. The retry still returns the
same `hasEmptySpots` answer, so the process continues consistently.

**Contrast with Base-Scenario:**
- **Base-scenario**: Retries → Multiple reservations → Spots vanish
- **Idempotency-pattern**: Retries → Skip already processed → Correct spot count

This demonstrates how tracking completed operations makes non-idempotent operations (like reserving a spot) safe in distributed systems with at-least-once delivery semantics.

## **Sequence Flow** 📊

Here's how the idempotency pattern works:

```mermaid
sequenceDiagram
    participant Zeebe
    participant Worker
    participant Service
    participant DB

    Zeebe->>Worker: 1. Trigger job (may be retry)
    Worker->>Worker: 2. Construct operationId<br/>(membershipId-elementId)
    Worker->>Service: 3. Call service with operationId
    Service->>DB: 4. Check if operationId exists<br/>in processed_operations

    alt Operation already processed
        DB-->>Service: 5a. Found
        Service-->>Worker: 6a. Skip execution, return
    else Operation not processed
        DB-->>Service: 5b. Not found
        Service->>DB: 6b. Execute business logic
        Service->>DB: 7b. Save operationId to<br/>processed_operations
        Service-->>Worker: 8b. Return
    end

    Worker-->>Zeebe: 9. Complete job
```

**Important**: All steps within the Service (check, execute, record) happen in a single database transaction, ensuring atomicity.

## **Advantages** 🎉

- **Prevents Duplicate Processing**: Handles Zeebe's at-least-once delivery safely
- **Business-Driven Keys**: OperationId based on domain entities + BPMN elements, not internal job IDs
- **Clean Architecture**: Idempotency logic in one central executor, services and workers stay thin
- **Atomic Pattern**: Check-execute-record happens in single transaction (no race conditions)
- **Simple Infrastructure**: Just a database table, no schedulers or message queues
- **Audit Trail**: `processed_at` timestamp provides history of when operations completed
- **Type Safety**: `OperationId` value object prevents string mistakes

## **Downsides** ⚠️

- **Limited Scope**: Only prevents duplicate worker executions, doesn't solve transaction coordination issues
- **Table Growth**: Every job execution creates a database entry
- **Cleanup Required**: Need strategy to archive/delete old entries (no automatic cleanup)
- **Not a Full Solution**: Must be combined with After-Transaction or Outbox patterns for complete safety
- **Performance Impact**: Additional database query per job execution
- **Memory Overhead**: processed_operations table can grow large in high-throughput systems

## **When to Use This Pattern?**

**Use this pattern when:**
- You're experiencing duplicate emails, notifications, or other side effects from Zeebe retries
- You need operation-level deduplication tied to business entities
- You want simple, database-backed idempotency without complex infrastructure
- You're combining with After-Transaction or Outbox patterns and need worker-level safety

**Don't use this pattern when:**
- You only need transaction coordination (use After-Transaction or Outbox instead)
- Your operations are naturally idempotent (no need for tracking)
- You can't afford the performance cost of extra database queries
- Table growth is a concern and you can't implement cleanup

## **Complementary Patterns**

This pattern works best **in combination** with other patterns:

| Pattern Combination | What It Solves |
|---------------------|----------------|
| **Idempotency only** | ❌ Duplicate workers, ❌ Transaction coordination issues |
| **After-Transaction + Idempotency** | ✅ Transaction safety + ✅ Duplicate prevention |
| **Outbox + Idempotency** | ✅ Reliable delivery + retries + ✅ Duplicate prevention |

**Recommended**: Use Idempotency Pattern with either After-Transaction or Outbox Pattern for production systems.

## **Conclusion**

The idempotency pattern provides a straightforward mechanism for preventing duplicate processing when Zeebe retries job workers. By tracking completed operations in a database table and implementing the Check → Execute → Record pattern at the service layer, you ensure that retried jobs don't cause duplicate side effects.

While it doesn't solve transaction coordination issues on its own, it's an essential complement to After-Transaction or Outbox patterns, providing defense-in-depth against duplicate processing in distributed systems.

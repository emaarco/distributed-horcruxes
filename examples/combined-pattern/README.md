# 🛡️ Combined Pattern Example (Outbox + Idempotency)

This example combines the [**Outbox Pattern**](../outbox-pattern/README.md) and the
[**Idempotency Pattern**](../idempotency-pattern/README.md) into a single, production-grade setup. Each pattern
on its own only covers one direction of the boundary between your application and Zeebe. Together they make the
whole round-trip safe:

- **Outbox** secures the **outgoing** path (service → Zeebe): messages are written to a database table inside the
  business transaction and delivered reliably by a scheduler with retries and `messageId` deduplication.
- **Idempotency** secures the **incoming** path (Zeebe → service): job workers record completed operations so that
  Zeebe's at-least-once retries never execute the same work twice.

## **The Problem It Solves** 🧩

Your service writes to its database and talks to Zeebe, two systems with their own commits and no coordinator
between them. That is the distributed transaction problem described in [CHALLENGES.md](../../CHALLENGES.md). The challenges show up on both sides: when the service messages Zeebe ([phantom instances](../../CHALLENGES.md#1-phantom-instance-process-runs-without-data-), [stale reads](../../CHALLENGES.md#2-premature-execution-reading-stale-data-), [overwritten data](../../CHALLENGES.md#3-premature-execution-overwriting-data-)) and when a
worker completes a job ([duplicate delivery](../../CHALLENGES.md#4-duplicate-delivery-), [lost job completions](../../CHALLENGES.md#5-job-completion-failure-connectivity-issues-)). This example covers challenges 1 to 5. The sixth, [cancelled jobs](../../CHALLENGES.md#6-job-completion-failure-task-no-longer-available-), needs
process modeling and compensation as in the [Saga Pattern](../saga-pattern/README.md).

## **Why Combine Them?** 🤔

A remote process engine like Zeebe manages its own state, independent of your database. Keeping the two in sync
requires solving problems on **both** sides of the boundary:

| Direction | Problem | Solved by |
|-----------|---------|-----------|
| Service → Zeebe | Phantom instances, stale reads, overwritten data (#1, #2, #3) | **Outbox** |
| Zeebe → Service | Duplicate delivery, lost job completions (#4, #5) | **Idempotency** |
| Service → Zeebe | Failed or repeated message sends | **Outbox retries + `messageId` dedup** |

Neither pattern alone is enough for a real system. The outbox guarantees a message *eventually* reaches Zeebe even
if delivery fails; idempotency guarantees that the worker triggered by that message runs its side effects exactly
once even if it is retried.

## **Overview** 🛠️

The example uses the same Inner Circle membership process as the other examples and wires the two mechanisms
together:

1. **Outbound (Outbox)** — `ProcessMessagePersistenceAdapter` implements the `MembershipProcess` port
   and writes a `PENDING` row into the `process_message` table inside the service transaction. The
   `ProcessEngineOutboxScheduler` polls every 200ms, sends messages to Zeebe with a unique `messageId`, and marks
   them `SENT` (or increments `retryCount` on failure).
2. **Inbound (Idempotency)** — every job worker builds a composite `OperationId` (`membershipId-elementId`) and
   the services wrap their business logic in a central `IdempotentOperationExecutor` that applies the
   **Check → Execute → Record** pattern against the `processed_operations` table, all inside one
   `@Transactional` boundary.

Because both the business data **and** the outbox message are persisted in the same transaction, they commit
atomically — the engine is never notified about data that was rolled back.

## **Code Example** 💻

### **Outbound: Writing to the Outbox in the Service Transaction**

The service stores the membership and the outbox message in a single transaction:

```kotlin
@Service
@Transactional
class RegisterMembershipService(
    private val repository: MembershipRepository,
    private val processPort: MembershipProcess, // -> ProcessMessagePersistenceAdapter
) : RegisterMembershipUseCase {

    override fun register(command: RegisterMembershipUseCase.Command): MembershipId {
        val membership = Membership(email = command.email, name = command.name)
        repository.save(membership)                     // business data
        processPort.submitRegistration(membership.id)   // outbox row (status = PENDING) - same transaction
        return membership.id
    }
}
```

The scheduler delivers the message reliably and uses a `messageId` so Zeebe deduplicates retries within the TTL:

```kotlin
private fun sendMessage(message: ProcessMessageEntity) {
    val variables = objectMapper.readValue(message.variables, object : TypeReference<Map<String, Any>>() {})
    val messageId = "${message.correlationId}-${message.messageName}"
    camundaClient.newPublishMessageCommand()
        .messageName(message.messageName)
        .correlationKey(message.correlationId)
        .messageId(messageId)                       // engine-level deduplication
        .variables(variables)
        .timeToLive(Duration.of(10, ChronoUnit.SECONDS))
        .send()
        .join()
}
```

### **Inbound: Idempotent Workers**

Workers construct the composite `OperationId`; the service wraps its business logic in the central
`IdempotentOperationExecutor`, which performs the **Check → Execute → Record** cycle in one place:

```kotlin
@JobWorker(type = ServiceTasks.MEMBERSHIP_SEND_WELCOME_MAIL)
fun sendWelcomeMail(job: ActivatedJob, @Variable("membershipId") membershipId: String) {
    useCase.sendWelcomeMail(
        MembershipId(UUID.fromString(membershipId)),
        OperationId("$membershipId-${job.elementId}"),
    )
}
```

```kotlin
@Component
class IdempotentOperationExecutor(
    private val processedOperationRepository: ProcessedOperationRepository
) {
    fun runOnce(operationId: OperationId, block: () -> Unit) {
        if (processedOperationRepository.existsById(operationId)) return // already done -> skip
        block()                                                          // execute
        processedOperationRepository.save(operationId)                   // record
    }
}
```

```kotlin
@Service
@Transactional
class SendWelcomeMailService(
    private val repository: MembershipRepository,
    private val idempotencyGuard: IdempotentOperationExecutor,
) : SendWelcomeMailUseCase {

    override fun sendWelcomeMail(membershipId: MembershipId, operationId: OperationId) {
        idempotencyGuard.runOnce(operationId) {
            val membership = repository.find(membershipId)
            log.info { "Sending welcome mail to ${membership.email.value}" }
        }
    }
}
```

Because `runOnce` is called inside the service's `@Transactional` boundary, check, business logic and record
still commit atomically.

## **Sequence Flow** 📊

```mermaid
sequenceDiagram
    participant Service
    participant DB
    participant Scheduler
    participant Zeebe
    participant Worker

    Note over Service,DB: Outbound path (Outbox)
    Service ->> DB: 1. Save business data + outbox row (PENDING)
    Service ->> DB: 2. Commit transaction (atomic)
    Scheduler ->> DB: 3. SELECT FOR UPDATE SKIP LOCKED (PENDING)
    Scheduler ->> Zeebe: 4. Publish message (with messageId)
    Scheduler ->> DB: 5. Mark SENT (or increment retryCount)

    Note over Zeebe,DB: Inbound path (Idempotency)
    Zeebe ->> Worker: 6. Trigger job (may be a retry)
    Worker ->> Worker: 7. Build operationId (membershipId-elementId)
    Worker ->> DB: 8. Check processed_operations
    alt Already processed
        Worker -->> Zeebe: 9a. Skip side effect, complete job
    else Not processed
        Worker ->> DB: 9b. Execute + record operationId (one transaction)
        Worker -->> Zeebe: 10b. Complete job
    end
```

## **Advantages** 🎉

- **Guaranteed delivery**: Messages survive failures via the outbox and are retried until sent.
- **No duplicate execution**: Idempotency checks plus `messageId` deduplication prevent double side effects.
- **Transactional consistency**: Business data and outbox message commit all-or-nothing.
- **Resilience**: Tolerates database failures, network issues, and Zeebe retries on both sides of the boundary.

## **Downsides** ⚠️

- **More moving parts**: A scheduler, an outbox table, and a `processed_operations` table to operate and monitor.
- **Latency**: The 200ms polling interval adds a small delay before messages reach Zeebe.
- **Table growth**: Both `process_message` (SENT rows) and `processed_operations` grow over time and need a cleanup
  strategy.
- **Unbounded retries**: Failing messages are retried indefinitely; add a max-retry / dead-letter strategy for
  production.

## **When to Use This Pattern?**

Use the combined pattern for **production systems** that need end-to-end safety: reliable delivery to Zeebe *and*
protection against duplicate job execution. If you only need one side of the guarantee, the standalone
[Outbox](../outbox-pattern/README.md) or [Idempotency](../idempotency-pattern/README.md) examples are simpler.

## **Conclusion**

The combined pattern is the natural production setup: the **Outbox** guarantees that every message reliably reaches
Zeebe as part of the business transaction, while **Idempotency** guarantees that the work triggered by those
messages happens exactly once. Together they close challenges 1 to 5 in a single, consistent example; the sixth needs process
modeling and compensation.

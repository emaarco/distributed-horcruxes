# ⚠️ Base Scenario (The Problem)

This example demonstrates **the distributed transaction problem** in its raw form, without any solution applied. This is the starting point that shows what can go wrong when coordinating database transactions with a process engine like Zeebe.

> **⚠️ WARNING**: This implementation is intentionally flawed to demonstrate the problems. Use the other examples (after-transaction or outbox-pattern) for production code.

## **Overview** 🛠️

In this base scenario, the service layer makes direct calls to Zeebe **within the same transaction** where database operations occur. This naive approach leads to several critical problems because the process engine starts executing tasks before the database transaction is committed, and may continue even if the transaction fails.

### **The Naive Implementation**

Here's what happens in this example:

```kotlin
@Service
@Transactional
class RegisterMembershipService(
    private val repository: MembershipRepository,
    private val processPort: MembershipProcess
) : RegisterMembershipUseCase {

    override fun register(command: RegisterMembershipUseCase.Command): MembershipId {
        val membership = Membership(email = command.email, name = command.name)
        repository.save(membership)  // 1. Save to database (not committed yet!)
        processPort.submitRegistration(membership.id)  // 2. Notify Zeebe immediately
        return membership.id  // 3. Transaction commits after this
    }
}
```

The `MembershipProcessAdapter` in this example calls Zeebe directly without any safety mechanisms:

```kotlin
@Component
class MembershipProcessAdapter(
    private val camundaClient: CamundaClient
) : MembershipProcess {

    override fun submitRegistration(id: MembershipId) {
        // PROBLEM: This call happens immediately, potentially before the DB commit!
        val variables = mapOf("membershipId" to id.value.toString())
        camundaClient.newPublishMessageCommand()
            .messageName("miravelo.registrationSubmitted")
            .withoutCorrelationKey()
            .variables(variables)
            .send()
            .join()
    }

    override fun confirmMembership(id: MembershipId) {
        // PROBLEM: This call happens immediately, potentially before the DB commit!
        camundaClient.newPublishMessageCommand()
            .messageName("miravelo.membershipConfirmed")
            .correlationKey(id.value.toString())
            .timeToLive(Duration.of(10, ChronoUnit.SECONDS))
            .send()
            .join()
    }
}
```

## **What Problems Does This Cause?** 🚨

### **1. Premature Execution**

The process engine starts tasks **before your database transaction is committed**.

- **Timeline**:
  1. Service saves membership to database (uncommitted)
  2. Service notifies Zeebe to start process
  3. Zeebe immediately activates job for "Claim membership"
  4. Worker picks up job and tries to load membership from database
  5. **Problem**: Membership data might not be visible yet (uncommitted)!

- **Result**: Workers fail because they can't find the data they need, requiring retries or manual intervention.

### **2. Out-of-Sync States: Engine Ahead of Database**

If the database transaction **fails after notifying Zeebe**, the process engine has already started but the database has no record.

- **Timeline**:
  1. Service saves membership to database (uncommitted)
  2. Service notifies Zeebe (succeeds)
  3. Database transaction fails and rolls back
  4. **Problem**: Zeebe thinks the membership exists, but database has nothing!

- **Result**: The process engine state is completely out of sync with the database.

### **3. Out-of-Sync States: Transaction Rollback After Success**

Even if both operations succeed individually, errors later in the transaction can roll back the database but not the process engine.

- **Timeline**:
  1. Service saves membership to database (uncommitted)
  2. Service notifies Zeebe (succeeds)
  3. Service does additional operations
  4. Additional operation throws exception
  5. Spring rolls back entire transaction
  6. **Problem**: Database rolled back, but Zeebe already started!

- **Result**: Zeebe proceeds with a process that has no corresponding database record.

### **4. Race Conditions**

In high-throughput scenarios, workers may pick up jobs before the transaction commits, even if it eventually succeeds.

- **Problem**: The timing window between "notify Zeebe" and "commit transaction" creates a race condition.
- **Result**: Intermittent failures that are hard to reproduce and debug.

## **How to Observe These Problems** 🔬

### **Setup**
1. Start the infrastructure: `cd stack && docker-compose up`
2. Run this example application (port 8081)
3. Monitor Operate at http://localhost:8080/operate (credentials: demo/demo)

### **Reproduce Premature Execution**
1. Add a sleep or delay before the transaction commits (simulate slow commit)
2. Send a registration request via Bruno or REST API
3. Observe in logs: Worker tries to load the membership before it's committed
4. See failed jobs in Operate

### **Reproduce Out-of-Sync States**
1. Inject a `RuntimeException` after the `processPort.submitRegistration()` call
2. Send a registration request
3. Observe: Zeebe starts the process, but database transaction rolls back
4. Check Operate: Process instance exists with no database record

### **Compare with Other Solutions**
Run the same requests against:
- `after-transaction` example (port 8081) - See how transaction manager prevents issues
- `outbox-pattern` example (port different) - See how outbox provides reliability

## **Sequence Flow** 📊

Here's what happens in the base scenario:

```mermaid
sequenceDiagram
    participant Service
    participant DB
    participant Engine
    participant Worker

    Service ->> DB: 1. Save membership (uncommitted)
    Service ->> Engine: 2. Start process (immediate!)
    Engine ->> Worker: 3. Activate job

    alt Worker executes before commit (PROBLEM)
        Worker ->> DB: 4a. Try to load membership
        DB -->> Worker: 4b. Data not visible (uncommitted)
        Note over Worker: Worker fails - data not available
    else Worker executes after commit (LUCKY)
        Service ->> DB: 4a. Commit transaction
        Worker ->> DB: 4b. Load membership
        DB -->> Worker: 4c. Data available
        Note over Worker: Worker succeeds (but timing is unreliable)
    end
```

## **Why This Matters** 💡

This base scenario represents a common mistake when integrating with external systems. Many developers intuitively write code this way without realizing the transaction boundaries and timing issues.

Understanding these problems is crucial because:
1. **They're subtle** - Often work fine in development but fail in production
2. **They're hard to debug** - Race conditions and timing-dependent failures
3. **They cause data inconsistency** - Database and process engine states drift apart
4. **They require proper solutions** - After-transaction hooks or outbox patterns

## **The Spot Idempotency Problem** 🔢

The Inner Circle has a limited number of spots. "Claim membership" reserves one of them in memory, which demonstrates another critical distributed transaction problem: **non-idempotent operations with retries**.

### **The Scenario**

The first task of the process is `membership.claimMembership`. Its worker reserves a spot and reports back whether one was available:

```kotlin
@JobWorker(type = "membership.claimMembership")
fun claimMembership(@Variable("membershipId") membershipId: String): Map<String, Boolean> {
    val hasEmptySpots = useCase.claim(membershipId)
    // Randomly fails after reserving the spot but before acknowledging to Zeebe
    if (Math.random() > 0.8) {
        throw RuntimeException("Simulating error on acknowledging")
    }
    return mapOf("hasEmptySpots" to hasEmptySpots)
}
```

### **The Problem**

The worker reserves the spot but randomly throws an exception **after** the reservation but **before** acknowledging job completion to Zeebe. This simulates real-world scenarios where:
- Network issues prevent acknowledgment
- Worker crashes after processing but before responding
- Timeouts occur after business logic completes

When this happens:
1. A spot is reserved (side effect completed)
2. Exception is thrown before acknowledgment
3. Zeebe never receives completion confirmation
4. Zeebe retries the job
5. A **second** spot is reserved for the same membership

**Result**: The Inner Circle fills up with phantom members, and later applicants are rejected although spots are free.

### **Why This Matters**

This demonstrates that **reserving a spot is not idempotent**. Running the same reservation multiple times produces different results each time. In production systems, non-idempotent operations like:
- Reserving capacity or incrementing counters
- Sending notifications
- Processing payments
- Creating audit logs

All suffer from this problem when combined with Zeebe's at-least-once delivery semantics.

### **The Solution**

See the [Idempotency Pattern](../idempotency-pattern/README.md) example for how to handle this using an operation log that tracks completed operations, ensuring the spot is reserved exactly once regardless of retries.

## **What Should You Use Instead?** ✅

This repository provides two battle-tested solutions:

1. **[After-Transaction Hook](../after-transaction/README.md)** ✅
   - Execute Zeebe calls only after database commit
   - Fast but no retry logic
   - Good for: Real-time requirements, simpler scenarios

2. **[Outbox Pattern](../outbox-pattern/README.md)** 📦
   - Store messages in database, send asynchronously
   - Reliable with retries
   - Good for: Mission-critical operations, eventual consistency acceptable

## **Conclusion**

This base scenario intentionally demonstrates the distributed transaction problem. **Never use this approach in production.** Instead, use one of the proven patterns demonstrated in the other examples, which properly handle the coordination between database transactions and process engine interactions.

For a detailed explanation of all the challenges, see [CHALLENGES.md](../../CHALLENGES.md) in the repository root.

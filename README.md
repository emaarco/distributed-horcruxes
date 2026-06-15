# 🧙‍♂️ Distributed Horcruxes

> 🚧 This repository is subject to constant change. It deals with a problem that is still rarely examined in depth in the
> context of **remote** process engines (like Zeebe / Camunda 8).

## **Introduction** 🗂

Distributed transactions are a common challenge in distributed systems. Whenever a single operation spans multiple
independent systems, failures—communication issues, partial updates, timeouts—can leave those systems in contradictory
states.

Much like horcruxes, distributed transactions are tricky to manage, dangerous if left unchecked, and can create chaos in
your system if not handled properly.

This is especially relevant when working with a **remote** process engine such as Zeebe, whose state lives in a separate
system from your application's database. Without the right strategies, workflows can drift out of sync with your data.
Note that this is a **trade-off, not a defect**: a remote engine exchanges coordination complexity for scalability,
resilience, and cloud-native deployment. The same challenges appear with any external system—APIs, message brokers, or
an embedded engine the moment it runs over the network.

This repository provides practical examples and proven patterns to handle these problems in a Spring Boot and Zeebe
environment.

## **The Distributed Transaction Problem** 🕵️

The problem is generic in software architecture and arises when one operation spans multiple systems, such as:

- A **database** for your business data.
- A **process engine** like Zeebe for orchestrating workflows.
- Other systems like **APIs** or **message brokers**.

Within a single database, the database acts as a transaction coordinator—everything commits or rolls back together.
Across independent systems, **no such coordinator exists**, so you can't guarantee they all succeed or fail together.
This leads to:

- **Inconsistent states** — some systems finish their work while others fail.
- **Duplicate actions** — retries create duplicate operations.
- **Data conflicts** — tasks execute out of order or on incomplete data.

A remote engine like Zeebe is itself a distributed system, designed for high availability and scalability. It manages
its own state independently from your database, creating a boundary that must stay synchronized. The most common symptom
is a **timing problem**: the engine assumes a task can start as soon as it's triggered, but your database transaction may
not be committed yet—or may fail during commit.

> 💡 **Want to understand these challenges in depth?**<br/>
> Read the [detailed breakdown of distributed transaction challenges](CHALLENGES.md)—from the general problem (ACID vs.
> BASE) to the specific failure modes when coordinating with a remote engine, with diagrams and a reference architecture.
> For the full narrative, see the [companion blog post](https://medium.com/p/d4bbbca295d6).

## **How Do We Solve This?** 🛠

These are well-known problems with well-tested solutions. They apply across distributed systems and adapt cleanly to
Zeebe. This repository explores the following patterns:

### **1. After-Transaction Hook** ✅

Trigger the engine only **after** the database transaction commits, avoiding notifications based on uncommitted data.
Simple, but offers no retry if the call then fails.

### **2. Outbox Pattern** 📦

Save engine messages in an "outbox" table within the **same** transaction. A background scheduler reliably delivers them
afterwards, with retries. Reliable, at the cost of added infrastructure and slight latency.

### **3. Idempotency Pattern** 🔁

Track completed operations so duplicate deliveries (at-least-once semantics) are processed only once. Uses a database
table that records which operations have already run.

### **4. Combined Pattern** 🛡️

Outbox **and** idempotency together—the outbox secures _your service → engine_, idempotency secures _engine → your
service_—for end-to-end safety on both sides of the boundary.

### **5. Saga Pattern** ⏪

Handle rollbacks of already-completed work using BPMN compensation events. When a later step fails, compensation
handlers undo previous operations.

These patterns offer different trade-offs between simplicity, reliability, and performance.

## **Examples in This Repository** 📚

Each example demonstrates either the problem or a solution:

- ⚠️ [**Base Scenario**](./examples/base-scenario/README.md):
  The naive implementation that demonstrates what goes wrong without proper transaction handling.
- ✅ [**After-Transaction Hook**](./examples/after-transaction/README.md):
  Ensuring engine interactions occur only after the transaction commits.
- 📦 [**Outbox Pattern**](./examples/outbox-pattern/README.md):
  Using a database outbox and scheduler for reliable message delivery.
- 🔁 [**Idempotency Pattern**](./examples/idempotency-pattern/README.md):
  Preventing duplicate processing from at-least-once delivery semantics.
- 🛡️ [**Combined Pattern**](./examples/combined-pattern/README.md):
  Outbox + Idempotency together for end-to-end transaction safety on both sides of the boundary.
- ⏪ [**Saga Pattern**](./examples/saga-pattern/README.md):
  Handling distributed transaction rollbacks using BPMN compensation events.

## **Setup** ⚙️

Getting started is simple. You'll need a **JDK 21+** and a container runtime (Docker or Podman).

**1: Start the Infrastructure**

The stack uses the consolidated **Camunda 8 orchestration cluster** (a single container bundling Zeebe, Operate, and
Tasklist) plus PostgreSQL and Elasticsearch. Bring it up from the `stack` folder:

```bash
cd stack
docker-compose up
```

**2: Run an Example**

Each example is a standard Spring Boot application. Run its `ExampleApplication.kt` main class from your IDE, or from the
command line:

```bash
./gradlew :examples:<pattern-name>:bootRun
# e.g. ./gradlew :examples:outbox-pattern:bootRun
```

> ⚠️ **Run one example at a time.** All examples bind port **8081**, except the **saga-pattern** which uses **8083**.
> Starting two on the same port will fail.

**3: Interact with the Process**

Each example uses the same newsletter subscription process. Predefined API calls live in the [bruno](bruno) directory
(open them with [Bruno](https://www.usebruno.com/), or replicate them with curl/Postman):

- `subscribe-to-newsletter.bru` — start a subscription (port 8081 examples).
- `confirm-subscription.bru` — confirm a subscription (port 8081 examples).
- `subscribe-to-payed-newsletter.bru` — start the paid-newsletter flow used by the **saga-pattern** (port 8083).

**4: Monitor the Processes**

Monitor and debug workflows in **Operate**, served by the orchestration cluster at
[http://localhost:8080/operate](http://localhost:8080/operate). Credentials are `demo/demo`.

## **Contribute to This Project** 🤝

Distributed transactions are tricky, but together we can solve them! If you have ideas, improvements, or new examples,
feel free to open a pull request or issue.

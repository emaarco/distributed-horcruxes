# 🧙‍♂️ Distributed Horcruxes

> 🚧 This repository is subject of constant change. It deals with a problem that has not yet been examined and solved in
> depth in the context of remote process engines (like Zeebe).

## **Introduction** 🗂

Distributed transactions are a common challenge in distributed systems.
When multiple services or systems need to coordinate to ensure consistent data,
failures—like communication issues or incomplete updates—can cause problems.

Much like horcruxes, distributed transactions are tricky to manage,
dangerous if left unchecked, and can create chaos in your system if not handled properly.

This is particularly relevant when working with external systems,
such as process engines like Zeebe or Camunda 7, where state is managed independently.
If proper strategies aren’t in place, workflows or interactions may lead to inconsistencies
between the process engine and your application’s database.

This repository aims to provide practical examples and proven patterns
to handle these distributed transaction problems effectively in a Spring Boot and Zeebe environment.

## **The Distributed Transaction Problem** 🕵️

Distributed transactions are a generic problem in software architecture that occurs when a single operation spans
multiple systems, such as:

- A **database** for storing application data.
- A **process engine** like Zeebe for orchestrating workflows.
- Other systems like **APIs** or **message brokers**.

Problems arise when we can’t guarantee that all systems succeed or fail together. This can lead to:

- **Inconsistent states**: Some systems finish their operations, while others fail.
- **Duplicate actions**: Systems retry failed tasks, causing duplicate operations.
- **Data conflicts**: Tasks execute out of order or with incomplete data.

## **The Challenge with Zeebe** ⚙️

Zeebe, as a distributed system itself, is designed for high availability and scalability.
This means it manages its own state independently from your application's database,
creating a boundary between two systems that must remain synchronized.

When coordinating these two independent systems, various challenges emerge.
One of the most common issues is a **timing problem**: Zeebe assumes that tasks can start as soon as they are triggered,
but your database transaction might not be committed yet—or might fail during the commit.

Consider this simple scenario:

1. You save data to the database and immediately notify Zeebe to start a process.
2. If the database transaction fails, Zeebe has already started tasks based on incomplete or incorrect data.

Without proper strategies, this can cause:

- Tasks to fail unexpectedly.
- The database and process engine states to drift apart.
- Retrying the same operation to produce duplicates.

> 💡 **Want to understand these challenges in more detail?**<br/>
> Check out the [detailed breakdown of distributed transaction challenges](CHALLENGES.md),
> which includes examples, diagrams, and a reference architecture to explain these problems in depth.

## **How Do We Solve This?** 🛠

Distributed transactions are a common problem in software architecture,
and luckily, there are some **well-known and tested solutions** to address them.
These solutions are applicable across many distributed systems and can also be adapted to work with Zeebe.

Here are some of the most effective patterns, we want to explore:

### **1. After-Transaction Hook** ✅

Trigger Zeebe only after the database transaction is successfully committed.
This avoids notifying Zeebe with incomplete or uncommitted data.

### **2. Outbox Pattern** 📦

Save Zeebe messages in an "outbox" table as part of the same database transaction.
A scheduler or worker then reliably sends these messages to Zeebe after the transaction is complete.

### **3. Idempotency Pattern** 🔁

Track completed operations to prevent duplicate processing when Zeebe retries job workers.
Uses a database table to record which operations have been completed.

### **4. Saga Pattern** ⏪

Handle distributed transaction rollbacks using BPMN compensation events.
When a later step fails, compensation handlers automatically undo previously completed operations.

These patterns are widely used in distributed systems
and provide different trade-offs between simplicity, reliability, and performance.

## **Examples in This Repository** 📚

This repository contains examples that demonstrate both the problem and proven solutions:

- ⚠️ [**Base Scenario**](./examples/base-scenario/README.md):
  The naive implementation that demonstrates what goes wrong without proper transaction handling.
- ✅ [**After-Transaction Hook**](./examples/after-transaction/README.md):
  Ensuring Zeebe interactions occur only after the transaction commits.
- 📦 [**Outbox Pattern**](./examples/outbox-pattern/README.md):
  Using a database outbox and scheduler for reliable message delivery.
- 🔁 [**Idempotency Pattern**](./examples/idempotency-pattern/README.md):
  Preventing duplicate processing from Zeebe's at-least-once delivery semantics.
- ⏪ [**Saga Pattern**](./examples/saga-pattern/README.md):
  Handling distributed transaction rollbacks using BPMN compensation events.

## **Setup** ⚙️

Getting started with the examples is simple! Follow these steps:

**1: Start the Infrastructure**:  
Navigate to the `stack` folder and bring up the infrastructure (Zeebe, Operate, etc.) using Docker Compose:

   ```bash
   cd stack
   docker-compose up
   ```

**2: Run the Example**:  
Go to the folder of the example you want to try and start the application by running the `ExampleApplication.kt` main
class.
Each example is a standard Spring Boot application,
so you can run it using your preferred IDE or command line.

**3: Interact with the Process**:  
Each example uses the same newsletter subscription process.
To interact with the process, you need to send requests to the REST API provided by the example services.

- To make this easier, there are predefined API call files located in the [bruno](bruno) directory.
- If you use [Bruno](https://www.usebruno.com/), simply open the folder and execute these files.
- Alternatively, you can use any other tool like curl or Postman to send the requests manually.

**4: Monitor the Processes**:  
Once the infrastructure is running, you can monitor and debug workflows using **Operate**
at [http://localhost:9091/operate](http://localhost:9091/operate).
The credentials are `demo/demo`.

## **Contribute to This Project** 🤝

Distributed transactions are tricky, but together we can solve them!
If you have ideas, improvements, or new examples,
feel free to contribute by opening a pull request or issue.

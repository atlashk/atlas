# Data management

## Data consistency

https://levelup.gitconnected.com/system-design-concepts-data-consistency-%EF%B8%8F-a7a3a0870275

### Eventual consistency

---

## Database Per Microservice Pattern

Problem:
A single database for all microservices creates bottlenecks and tight coupling.

Solution:
Each microservice has its own database, improving scalability and independence.

Best Practices:
✔ Use polyglot persistence (choose the best database for each service).
✔ Avoid cross-service joins — use APIs instead.

---

## Distributed transaction

### Saga pattern

A Saga is a sequence of local transactions where each transaction updates the database and publishes an event to trigger the next step in the workflow. If a failure occurs at any step, compensating transactions are executed to roll back the changes made by previous steps.

There are two primary ways to implement the Saga Pattern:

1. Choreography-Based Saga (Event-Driven)
   In this approach, each service listens to domain events from other services and executes its local transaction accordingly.
   Services communicate through an event broker (e.g., Kafka, RabbitMQ, or AWS MSK).
   No central orchestrator controls the workflow.
   ✔ Advantages:
    - Reduces coupling between services.
    - Easier to scale. 
    - Best for simple workflows. 
   ❌ Disadvantages:
    - Hard to manage complex workflows. 
    - Debugging and tracing failures can be difficult.
2. Orchestration-Based Saga (Centralized Controller)
   A central Saga Orchestrator (e.g., a dedicated service) coordinates the sequence of transactions.
   The orchestrator invokes services and listens for success or failure responses.
   It triggers compensating transactions if a failure occurs.
   ✔ Advantages:
   - Easier to implement complex workflows.
   - Better monitoring and debugging. 
   ❌ Disadvantages:
   - Introduces a single point of failure. 
   - Slightly higher coupling compared to choreography.

---

## Dual-writes

### Outbox pattern

Disadvantages:
- First, implementing the outbox pattern requires developers to design and maintain the outbox table, manage its cleanup, and ensure that asynchronous processing works reliably. Mistakes in handling the outbox, such as using overly complicated locking mechanisms or mismanaging the data flow, can introduce bugs and inefficiencies. 
- Additionally, the operational overhead is high, as the outbox pattern depends on additional processes to query the outbox table, publish messages, and then delete the entries. This can lead to increased resource consumption and latency compared to a direct database-to-queue approach.

### CDC

---

## Race-condition

### SQL constraint

### Pessimistic locking + Unique key

### Optimistic locking + Unique key

### Redis locking

| Feature/Aspect         | Redis Lock                                                                 | Optimistic Locking                                                                 | Pessimistic Locking                                                               |
|------------------------|----------------------------------------------------------------------------|------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------|
| **Usage Context**      | Distributed systems where multiple processes/servers or databases need to coordinate. | Scenarios with high contention but low likelihood of conflicts.                     | Scenarios with a high likelihood of conflicts or critical sections.               |
| **Concurrency**        | Allows multiple processes/servers to attempt to acquire the lock.         | Multiple transactions proceed in parallel, and conflicts are detected at commit time. | Only one transaction can access the resource at a time.                          |
| **Performance**        | Moderate latency due to network round-trips to Redis.                     | High performance is due to less waiting, but there is potential for retries.        | Lower performance due to increased waiting time.                                  |
| **Failure Handling**   | Requires handling for lock expiration and ensuring atomicity using mechanisms like Lua scripts. | Requires handling for retries on conflict detection.                               | Straightforward, but needs to handle deadlocks and potentially long waits.        |
| **Implementation Complexity** | Moderate; involves network calls and handling Redis-specific details.       | Moderate; involves adding version/timestamp checks in the code.                    | Simple to implement but can complicate transaction management.                    |
| **Scalability**        | Highly scalable due to Redis' ability to handle high loads.               | Highly scalable; minimal contention and no centralized locking mechanism.           | Less scalable due to potential bottlenecks with high contention.                 |

---

## Idempotency

https://levelup.gitconnected.com/how-to-avoid-double-payments-in-distributed-systems-bd15234e1d66

# Java Concurrency

## Thread pool

### `CachedThreadPool`

Key Characteristics of `CachedThreadPool`:
1. Dynamic Thread Management:
- The pool creates new threads as needed, but it will reuse existing threads if they are available.
- If a thread is idle for 60 seconds, it will be terminated and removed from the pool.
2. Unbounded Thread Pool:
- The number of threads in the pool can grow indefinitely, depending on the number of tasks submitted.
- This can be useful for handling a large number of short-lived tasks
3. Automatic Thread Reuse:
- Threads that have finished executing a task are returned to the pool and can be reused for subsequent tasks
4. No Queueing:
- Tasks are not queued; instead, they are immediately executed by an available thread or a new thread is created if none are available

When to Use `CachedThreadPool`:
1. Short-lived tasks: If you have many short-lived tasks that can be executed concurrently, a CachedThreadPool can be a good choice.
2. Unpredictable workload: If the number of tasks is unpredictable and can vary over time, a CachedThreadPool can dynamically adjust the number of threads.

When Not to Use CachedThreadPool:
1. Long-Running Tasks: If tasks take a long time to complete, the pool might create too many threads, leading to resource exhaustion.
2. Strict Resource Limits: If you need to limit the number of threads (e.g., to avoid overloading the system), a `FixedThreadPool` might be a better choice.

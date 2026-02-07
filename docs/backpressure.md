# Backpressure

Backpressure handling in distributed systems is critical to ensure that systems can manage load appropriately, preventing service failures under high traffic conditions. Backpressure occurs when a system or service cannot process incoming requests as fast as they are arriving, which can result in overload, failures, or data loss.

Below are several techniques to handle backpressure in distributed systems, along with the relevant implementation considerations and a block diagram to visualize each approach.

##  Rate Limiting (Token Bucket)

https://miro.medium.com/v2/resize:fit:1100/format:webp/1*aj_1wcA3O-En2pOanCcrFA.png

The Token Bucket Algorithm is one of the most common rate-limiting techniques. The idea is to control the rate at which requests are processed by maintaining a “bucket” of tokens. Each request consumes a token, and tokens are refilled at a constant rate. If the bucket is empty (i.e., no tokens are available), requests are either delayed or rejected, thereby protecting the system from being overloaded.

Use Cases:
- API Gateways: Preventing abuse by limiting the number of requests a user can make within a certain time frame.
- Rate Limiting for Distributed Systems: Ensures that downstream services are not overwhelmed by high traffic.

Key Characteristics:
- Token Generation Rate: Tokens are added to the bucket at a fixed rate.
- Capacity: The bucket has a fixed capacity. Once full, new tokens are discarded.
- Request Handling: Requests consume tokens. If no tokens are available, the request is either delayed or rejected.

```java
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TokenBucketRateLimiter {
    private final int maxTokens;
    private final AtomicInteger currentTokens;
    private long lastRefillTimestamp;
    private final long refillIntervalMillis;
    private final int refillTokens;

    public TokenBucketRateLimiter(int maxTokens, int refillTokens, long refillInterval, TimeUnit timeUnit) {
        this.maxTokens = maxTokens;
        this.currentTokens = new AtomicInteger(maxTokens);
        this.refillTokens = refillTokens;
        this.refillIntervalMillis = timeUnit.toMillis(refillInterval);
        this.lastRefillTimestamp = System.currentTimeMillis();
    }

    private void refill() {
        long now = System.currentTimeMillis();
        long timeElapsed = now - lastRefillTimestamp;
        
        if (timeElapsed > refillIntervalMillis) {
            int tokensToAdd = (int) (timeElapsed / refillIntervalMillis) * refillTokens;
            currentTokens.set(Math.min(maxTokens, currentTokens.get() + tokensToAdd));
            lastRefillTimestamp = now;
        }
    }

    public synchronized boolean tryConsume() {
        refill();
        if (currentTokens.get() > 0) {
            currentTokens.decrementAndGet();
            return true;
        }
        return false; // Rate limit exceeded, request rejected
    }
}
```

---

## Load Shedding

https://miro.medium.com/v2/resize:fit:1100/format:webp/1*ilnc4bzwNY2i48hTKNrHzA.png

Load shedding is a backpressure technique where the system intentionally drops or rejects excess requests when it becomes overloaded. This helps protect the system’s core services from degradation. Load shedding is typically used when the system can’t keep up with incoming traffic, and some requests need to be discarded in favor of ensuring the system stays operational.

In distributed systems, this technique is useful when the system is under extreme load. By shedding less important or non-critical requests, we preserve resources for the most critical ones.

**Use Cases:**
- Web Servers: When overwhelmed, shed non-critical requests to keep the system responsive.
- Message Queues: When queues overflow, drop less important messages to ensure priority messages are processed.
- Microservices: Handle overload situations by shedding load in microservices that are under heavy traffic.

**Key Characteristics:**
- Threshold-Based Shedding: The system drops requests once the request rate crosses a pre-defined threshold.
- Priority Handling: Systems often assign priorities to requests and shed low-priority ones.
- Graceful Degradation: The system avoids crashing by shedding excess load instead of failing entirely.

```java
import java.util.concurrent.atomic.AtomicInteger;

public class LoadShedding {
    private final int requestThreshold;
    private AtomicInteger currentRequests;

    public LoadShedding(int requestThreshold) {
        this.requestThreshold = requestThreshold;
        this.currentRequests = new AtomicInteger(0);
    }

    public boolean tryProcessRequest() {
        if (currentRequests.incrementAndGet() <= requestThreshold) {
            // Process request
            System.out.println("Request processed.");
            currentRequests.decrementAndGet(); // After processing, decrement the count
            return true;
        } else {
            // Load shedding - reject request
            System.out.println("Request rejected (load shedding).");
            currentRequests.decrementAndGet();
            return false;
        }
    }
}
```

---

## Circuit Breaking

https://miro.medium.com/v2/resize:fit:1100/format:webp/1*tp6qjyh5RIrGNoRAa7y3Rg.png

Circuit breaking is a backpressure handling technique where the system temporarily “breaks” or blocks requests to a dependent service when it detects that the service is failing or underperforming. This protects the overall system from cascading failures or slowdowns caused by waiting on unresponsive services.

The circuit breaker can have three states:
1. Closed: The service is functioning normally, and all requests are allowed.
2. Open: The service is failing, so requests are blocked or rejected without being forwarded.
3. Half-Open: The circuit breaker allows a limited number of requests through to see if the service has recovered. If successful, the breaker returns to the closed state; if not, it returns to the open state.

**Key Characteristics:**
- Failure Threshold: When the number of failures exceeds a threshold, the circuit breaker opens.
- Recovery: After a timeout period, the circuit breaker enters a half-open state to test if the service has recovered.
- Health Check: The circuit periodically tests whether the underlying service is operational.

**Use Cases:**
- Microservices: If one microservice is slow or down, using a circuit breaker prevents other microservices from being affected.
- API Gateways: Prevent overloading backend services by failing fast when an external service is unavailable.

```java
public class CircuitBreaker {
    private enum State { CLOSED, OPEN, HALF_OPEN }

    private State state = State.CLOSED;
    private int failureCount = 0;
    private final int failureThreshold;
    private final long timeout;
    private long lastFailureTime;

    public CircuitBreaker(int failureThreshold, long timeout) {
        this.failureThreshold = failureThreshold;
        this.timeout = timeout;
    }

    public boolean allowRequest() {
        switch (state) {
            case OPEN:
                if (System.currentTimeMillis() - lastFailureTime > timeout) {
                    state = State.HALF_OPEN; // Try to recover
                    return true; // Allow a few requests to test
                }
                return false; // Still open, reject requests
            case HALF_OPEN:
                return true; // Let requests go through in half-open state
            case CLOSED:
            default:
                return true; // Normal operation
        }
    }

    public void recordSuccess() {
        if (state == State.HALF_OPEN) {
            state = State.CLOSED; // Service recovered
        }
        failureCount = 0; // Reset failure count
    }

    public void recordFailure() {
        failureCount++;
        lastFailureTime = System.currentTimeMillis();
        if (failureCount >= failureThreshold) {
            state = State.OPEN; // Open circuit
        }
    }
}
```

---

## Buffering

https://miro.medium.com/v2/resize:fit:1100/format:webp/1*JXV_QT6INnjKs26aK_O60A.png

Buffering is a technique used to temporarily store incoming data or requests in a queue (buffer) when the system is unable to immediately process them. This prevents overload situations by smoothing out spikes in traffic and allowing the system to process requests at its own pace. Once the system catches up, it drains the buffer by processing the queued data.

Buffering is commonly used in distributed systems where different parts of the system operate at different speeds. It acts as a temporary storage mechanism between producers and consumers, preventing the system from becoming overwhelmed during periods of high load.

**Key Characteristics:**
- Temporary Storage: Incoming requests or data are held in a buffer (queue) until they can be processed.
- Smoothing Load Spikes: Buffers absorb sudden spikes in traffic, allowing the system to process them gradually.
- Rate Matching: Helps match the rate of data production and consumption in systems with varying speeds.

**Use Cases:**
- Message Queues: Systems like Kafka or RabbitMQ use buffering to store messages before they are consumed by downstream services.
- Web Servers: Buffer requests to prevent overloading backend services during traffic spikes.
Streaming Systems: Buffer incoming data streams to process them smoothly.

```java
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class BufferingSystem {
    private final BlockingQueue<String> buffer;
    private final int bufferCapacity;

    public BufferingSystem(int bufferCapacity) {
        this.bufferCapacity = bufferCapacity;
        this.buffer = new LinkedBlockingQueue<>(bufferCapacity);
    }

    public void produce(String request) {
        try {
            buffer.put(request); // Block if buffer is full
            System.out.println("Request added to buffer: " + request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void consume() {
        try {
            String request = buffer.take(); // Block if buffer is empty
            System.out.println("Processing request: " + request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        BufferingSystem system = new BufferingSystem(10);

        // Producer Thread
        new Thread(() -> {
            for (int i = 1; i <= 20; i++) {
                system.produce("Request-" + i);
            }
        }).start();

        // Consumer Thread
        new Thread(() -> {
            for (int i = 1; i <= 20; i++) {
                system.consume();
                try {
                    Thread.sleep(500); // Simulating processing delay
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }
}
```

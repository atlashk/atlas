# Fault-Tolerance

## Retry

https://levelup.gitconnected.com/system-design-concepts-retry-and-backoff-strategies-in-distributed-systems-f3d3cdfedb9d

---

## Circuit breakers

Circuit breakers insulate a service from its dependencies by preventing remote calls when a dependency is determined to be unhealthy, just as electrical circuit breakers protect homes from burning down due to excessive use of power. Circuit breakers  are implemented as state machines. When in their closed state, calls are simply passed through to the dependency. If any of these calls fails, the failure is counted. When the failure count reaches a specified threshold within a specified time period, the circuit trips into the open state. In the open state, calls always fail immediately. After a predetermined period of time, the circuit transitions into a “half-open” state. In this state, calls are again attempted to the remote dependency. Successful calls transition the circuit breaker back into the closed state, while failed calls return the circuit breaker to the open state.

---

## Bulkheads

---

## Timeout

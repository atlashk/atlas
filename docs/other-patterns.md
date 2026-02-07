# Other Patterns

## Sidecar pattern

Real-World Use Case:
Netflix wants to add security monitoring and logging to its microservices without modifying existing services.

Problem:
Adding logging, security, or monitoring to all services increases complexity.

Solution:
Use the Sidecar Pattern to deploy auxiliary services alongside main microservices.

How It Works:
1️⃣ Each microservice runs with a sidecar service (e.g., logging, monitoring).
2️⃣ The sidecar handles security, logging, or caching without modifying the core service.

Best Practices:
✔ Use Envoy or Istio for sidecar proxies.
✔ Deploy sidecars in Kubernetes pods.

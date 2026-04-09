# Atlas Helm Charts

An **umbrella chart** that composes all Atlas subcharts. Each layer is deployed into its own dedicated Kubernetes namespace for better isolation, security, and independent lifecycle management.

```
charts/
├── Chart.yaml        ← Umbrella chart definition (lists all subcharts as dependencies)
├── values.yaml       ← Single values file for ALL subcharts
├── common/           ← Shared RBAC: Namespace, ServiceAccount, Role, RoleBinding
├── infra/            ← Infrastructure layer (namespace: infra)
│                         mysql, postgres, kafka, rabbitmq, redis, elasticsearch,
│                         minio, qdrant, smtp4dev
├── observability/    ← Observability layer (namespace: observability)
│                         prometheus, grafana, loki, promtail, tempo,
│                         otel-collector, zipkin
├── security/         ← Security layer (namespace: security)
│                         keycloak
└── app/              ← Application layer (namespace: app)
                          api-gateway, authorization-server,
                          user-service, catalog-service, inventory-service,
                          order-service, payment-service
```

---

## Namespace Layout

| Subchart        | Namespace           | Contents |
|-----------------|---------------------|----------|
| `common`        | _(all namespaces)_  | RBAC — ServiceAccount, Role, RoleBinding |
| `infra`         | `infra`       | MySQL, PostgreSQL, Kafka, RabbitMQ, Redis, Elasticsearch, MinIO, Qdrant, SMTP4Dev |
| `security`      | `security`    | Keycloak |
| `observability` | `observability` | Prometheus, Grafana, Loki, Promtail, Tempo, OpenTelemetry Collector, Zipkin |
| `app`           | `app`         | API Gateway, Authorization Server, User, Catalog, Inventory, Order, Payment services |

---

## Prerequisites

- Kubernetes cluster ≥ 1.25
- Helm ≥ 3.10

---

## Installation

### 1. Resolve subchart dependencies

```bash
helm dependency update ./charts
```

### 2. Install each layer into its own namespace

```bash
# Infrastructure layer
helm install atlas-dev ./charts \
  --namespace infra --create-namespace \
  --set common.enabled=false \
  --set app.enabled=false \
  --set observability.enabled=false \
  --set security.enabled=false

# Security layer
helm install atlas-dev ./charts \
  --namespace security --create-namespace \
  --set common.enabled=false \
  --set app.enabled=false \
  --set infra.enabled=false \
  --set observability.enabled=false

# Observability layer
helm install atlas-dev ./charts \
  --namespace observability --create-namespace \
  --set common.enabled=false \
  --set app.enabled=false \
  --set infra.enabled=false \
  --set security.enabled=false

# Application layer (common RBAC is included here)
helm install atlas-dev ./charts \
  --namespace app --create-namespace \
  --set infra.enabled=false \
  --set observability.enabled=false \
  --set security.enabled=false
```

---

## Upgrading

```bash
# Upgrade the application layer with a new image tag
helm upgrade atlas-dev ./charts \
  --namespace app \
  --set infra.enabled=false \
  --set observability.enabled=false \
  --set security.enabled=false \
  --set app.global.image.tag=2.0.0

# Upgrade the infrastructure layer (e.g. bump MySQL image)
helm upgrade atlas-dev ./charts \
  --namespace infra \
  --set common.enabled=false \
  --set app.enabled=false \
  --set observability.enabled=false \
  --set security.enabled=false \
  --set mysql.image.tag=8.4
```

---

## Key design decisions

| Concern | Solution |
|---|---|
| **Per-layer namespaces** | Each subchart (`infra`, `security`, `observability`, `app`) is installed into its own dedicated namespace (`infra`, `security`, `observability`, `app`) for isolation and independent lifecycle. |
| **Single values file** | All values live in `charts/values.yaml`. Keys are flat (e.g. `mysql.*`, `kafka.*`) and shared across subcharts. |
| **Selective install** | Each subchart has an `enabled` condition in `Chart.yaml` — set to `false` to skip it during install/upgrade. |
| **Common RBAC** | The `common` subchart provisions the `ServiceAccount`, `Role`, and `RoleBinding` (`namespace-reader`). It is typically deployed alongside the `app` subchart. |
| **Cross-namespace service refs** | When using separate namespaces, app services reference infra/security/observability by their full DNS name: `<release>-<svc>.<namespace>.svc.cluster.local`. |
| **ServiceAccount** | App pods use `serviceAccountName: <release>-namespace-reader` (provisioned by `common`). |
| **Prometheus scrape targets** | `observability/files/prometheus/config.yml.tpl` uses `{{ lower $.Release.Name }}-<svc>` to build scrape target URLs for app services. |
| **Grafana datasources** | `datasources.yml.tpl` uses `{{ lower $.Release.Name }}-loki/prometheus/tempo` — all within the same `observability` release. |
| **Promtail → Loki** | `promtail/daemonset.yaml` uses `{{ lower $.Release.Name }}-loki` — same release, no cross-chart reference needed. |
| **ALB Ingress (AWS)** | The `api-gateway` supports optional ALB ingress via `apiGateway.ingress.enabled=true` with configurable `scheme` and `certificateArn`. |

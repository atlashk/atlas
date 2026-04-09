# Atlas Helm Charts

Five independent Helm charts for deploying the Atlas platform onto Kubernetes.

```
helm-charts/
├── atlas-common/        ← RBAC: ServiceAccount, Role, RoleBinding
├── atlas-data/          ← Data layer: databases, messaging, storage, identity
│                           (mysql, postgres, kafka, rabbitmq, redis, elasticsearch,
│                            keycloak, minio, qdrant, smtp4dev)
├── atlas-observability/ ← Observability stack: metrics, logging, tracing
│                           (prometheus, grafana, loki, promtail, tempo, otel-collector, zipkin)
└── atlas-app/           ← Microservices: api-gateway, catalog-service, order-service, ...
```

---

## Prerequisites

- Kubernetes cluster ≥ 1.25
- Helm ≥ 3.10
- All charts must be installed **in the same namespace**.

---

## Installation order

```
atlas-common  →  atlas-data  →  atlas-observability  →  atlas-app
```

### Step 1 – atlas-common

```bash
helm install atlas-common ./atlas-common \
  --namespace atlas \
  --create-namespace
```

### Step 2 – atlas-data

```bash
helm install atlas-data ./atlas-data \
  --namespace atlas
```

### Step 3 – atlas-observability

```bash
helm install atlas-observability ./atlas-observability \
  --namespace atlas \
  --set appReleaseName=atlas-app
```

> `appReleaseName` tells Prometheus which service hostnames to scrape from the app chart.

### Step 4 – atlas-app

```bash
helm install atlas-app ./atlas-app \
  --namespace atlas \
  --set commonReleaseName=atlas-common \
  --set dataReleaseName=atlas-data \
  --set observabilityReleaseName=atlas-observability
```

---

## Upgrading independently

```bash
# Redeploy only app services (new image tag)
helm upgrade atlas-app ./atlas-app \
  --namespace atlas \
  --set global.image.tag=2.0.0 \
  --set commonReleaseName=atlas-common \
  --set dataReleaseName=atlas-data \
  --set observabilityReleaseName=atlas-observability

# Upgrade only data layer (e.g. postgres version bump)
helm upgrade atlas-data ./atlas-data \
  --namespace atlas \
  --set postgres.image.tag=15

# Upgrade only observability stack
helm upgrade atlas-observability ./atlas-observability \
  --namespace atlas \
  --set appReleaseName=atlas-app
```

---

## Key design decisions

| Concern | Solution |
|---|---|
| **Data service naming** | Data services named `<dataReleaseName>-<svc>` (e.g. `atlas-data-mysql`). App chart references via `dataReleaseName` + `atlas.infra.*` helpers. |
| **Observability service naming** | Observability services named `<observabilityReleaseName>-<svc>` (e.g. `atlas-observability-tempo`). App chart references via `observabilityReleaseName` + `atlas.infra.*` helpers. |
| **ServiceAccount** | App pods use `serviceAccountName: <commonReleaseName>-namespace-reader`. |
| **Secrets cross-reference** | App deployments reference Secrets by name (e.g. `atlas-data-mysql`). Kubernetes Secrets are namespace-scoped and visible to all pods in the same namespace. |
| **Prometheus scrape targets** | `atlas-observability/files/prometheus/config.yml.tpl` uses `{{ .Values.appReleaseName }}-<svc>` to build scrape target URLs for app services. |
| **Grafana datasources** | `datasources.yml.tpl` uses `{{ lower $.Release.Name }}-loki/prometheus/tempo` — all within the same `atlas-observability` release, no cross-chart reference needed. |
| **Promtail → Loki** | `promtail/daemonset.yaml` wait-for uses `{{ lower $.Release.Name }}-loki` — same release, no cross-chart reference. |

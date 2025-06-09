# Atlas Kubernetes Deployment

This directory contains optimized Kubernetes manifests and scripts for deploying the Atlas microservices platform locally.

## Directory Structure

```
k8s/
├── base/                           # Base Kustomize configurations
│   ├── infrastructure/            # Infrastructure services
│   │   ├── mysql/
│   │   ├── redis/
│   │   ├── kafka/
│   │   └── rabbitmq/
│   ├── services/                  # Application services
│   │   ├── user-service/
│   │   ├── product-service/
│   │   ├── order-service/
│   │   └── notification-service/
│   ├── edge/                      # Edge services
│   │   ├── api-gateway/
│   │   ├── auth-server/
│   │   └── discovery-server/
│   └── observability/             # Monitoring and observability
│       ├── prometheus/
│       ├── grafana/
│       ├── zipkin/
│       └── loki/
├── overlays/                      # Environment-specific overlays
│   ├── local/                     # Local development
│   ├── dev/                       # Development environment
│   └── staging/                   # Staging environment
├── helm/                          # Helm charts (optional)
├── scripts/                       # Deployment scripts
└── config/                        # ConfigMaps and Secrets
```

## Quick Start

### Prerequisites
- Minikube or local Kubernetes cluster
- kubectl
- kustomize (optional, included in kubectl 1.14+)

### Deploy Infrastructure Services
```bash
./scripts/deploy-infrastructure.sh
```

### Deploy Application Services
```bash
./scripts/deploy-services.sh
```

### Deploy Observability Stack
```bash
./scripts/deploy-observability.sh
```

### Deploy Everything
```bash
./scripts/deploy-all.sh
```

## Features

- **Modular Structure**: Separated by service type and environment
- **Kustomize Support**: Environment-specific configurations
- **Resource Optimization**: Proper resource requests/limits
- **Health Checks**: Comprehensive liveness and readiness probes
- **Persistent Storage**: Proper PVC management
- **Security**: Secret management and RBAC
- **Observability**: Full monitoring and logging stack

## Environment Management

Use overlays for different environments:

```bash
# Local development
kubectl apply -k overlays/local

# Development environment
kubectl apply -k overlays/dev
```

## Monitoring

Access monitoring dashboards:
- Grafana: http://localhost:3000 (admin/admin)
- Prometheus: http://localhost:9090
- Zipkin: http://localhost:9411

## Troubleshooting

### Check pod status
```bash
kubectl get pods -A
```

### View logs
```bash
kubectl logs -f deployment/user-service
```

### Debug services
```bash
kubectl describe service user-service
``` 
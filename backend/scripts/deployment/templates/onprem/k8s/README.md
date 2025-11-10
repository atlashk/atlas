# Atlas - Kubernetes Deployment

This directory contains Kubernetes deployment configuration for Atlas microservices platform.

The Kubernetes deployment includes:
- **Microservices**: User, Product, Order, Notification applications
- **Infrastructure**: API Gateway, Auth Server, Config Server, Discovery Server
- **Databases**: MySQL, Redis
- **Messaging**: Apache Kafka
- **Observability**: Prometheus, Grafana, Zipkin, Loki, Promtail
- **Development Tools**: SMTP4Dev for email testing

---

## Prerequisites

- **Minimum memory** - 8GB
- **Kubernetes cluster** (minikube, kind, Docker Desktop, etc.)
- **kubectl** configured and connected to your cluster
- **Java 17+** - For building the project (if not skipping builds)
- **[Lens IDE](https://k8slens.dev/)** (optional but highly recommended for monitoring)

---

## Quick Start

All deployment scripts are located in this directory.

```bash
# Navigate to Kubernetes deployment directory
cd backend/scripts/deploy/onprem/k8s

# Start services
./deploy.sh                          # Local environment (default)
./deploy.sh --env local              # Specified environment

# Start services (skip builds)
./deploy.sh --skip-build             # Local environment (default)
./deploy.sh --env local --skip-build # Specified environment

# Stop services: scales to 0 replicas, preserves all data
./stop.sh                           # Local environment (default)
./stop.sh --env local               # Specified environment

# Clean up resources
./cleanup.sh                          # Local environment (default)
./cleanup.sh --env local              # Specified environment
```

---

## Environment Support

The deployment supports multiple environments, each running in its own namespace:
- **local** (default)
- **dev**
- **stg**
- **prod**

---

## Access Methods

### Option 1: Access Services via Ingress (Recommended)

After setting up Ingress, access services using local hostnames:
- **API Gateway**: http://api.atlas.local
- **Grafana**: http://grafana.atlas.local (admin/123456)
- **Prometheus**: http://prometheus.atlas.local
- **Zipkin**: http://zipkin.atlas.local
- **SMTP4Dev**: http://smtp4dev.atlas.local

### Option 2: Access Services via Port Forwarding

```bash
# API Gateway
kubectl port-forward -n atlas-local svc/api-gateway 8080:8080

# Grafana
kubectl port-forward -n atlas-local svc/grafana 3000:3000

# Prometheus
kubectl port-forward -n atlas-local svc/prometheus 9090:9090

# Zipkin
kubectl port-forward -n atlas-local svc/zipkin 9411:9411

# Smtp4dev
kubectl port-forward -n atlas-local svc/smtp4dev 80:80
```

---

## Monitoring with Lens IDE

**[Lens](https://k8slens.dev/)** is the most popular Kubernetes IDE with over 1 million users worldwide. It provides an intuitive, context-aware UI for managing and troubleshooting Atlas workloads.

### Installation:
1. Download Lens from [k8slens.dev](https://k8slens.dev/)
2. Install and launch the application
3. Connect to your cluster (automatically detects your kubectl context)

---

## Troubleshooting

- Verify your kubectl context is set to the correct cluster
- Ensure your cluster has sufficient resources (minimum 8GB memory)
- Check pod status: `kubectl get pods -n atlas-local`
- View logs: `kubectl logs -n atlas-local <pod-name>`
- Use Lens IDE for comprehensive cluster monitoring and debugging

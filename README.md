# Atlas

## Project Overview

**Atlas** is a comprehensive microservices-based e-commerce platform built with modern technologies and best practices. It demonstrates Domain-Driven Design (DDD), Event-Driven Architecture, and Clean Architecture principles across multiple bounded contexts including User Management, Product Catalog, Order Processing, and Notifications.

---

## Technical Stack

### Backend

- **Java 17** - Core programming language
- **Spring Boot 3.4.0** - Main application framework
- **Spring Framework 6.2.0** - Core Spring framework
- **Spring Cloud 2024.0.0** - Microservices infrastructure
- **MySQL 8.0** - Primary database
- **Redis 7** - Caching and session storage
- **Apache Kafka 7.9.0** - Event streaming and messaging
- **Gradle** - Build automation tool

### Frontend

- **Vue.js 3.5.14** - Progressive web framework
- **TypeScript 5.8.0** - Type-safe JavaScript
- **Vite 6.3.5** - Build tool and dev server
- **Pinia 3.0.1** - State management
- **Vue Router 4.5.1** - Client-side routing
- **Axios 1.9.0** - HTTP client

### Deployment

- **Docker & Docker Compose** - Containerization
- **Kubernetes** - Container orchestration (optional)

### Observabilities

- **Promptail** & **Loki** - Log aggregation
- **Prometheus** - Metrics
- **Zipkin** - Distributed tracing
- **Grafana** - Visualization and monitoring dashboards

### Architecture Patterns

- **Microservices Architecture** - Service decomposition
- **Domain-Driven Design (DDD)** - Business logic organization
- **Event-Driven Architecture** - Asynchronous communication
- **CQRS** - Command Query Responsibility Segregation
- **Outbox Pattern** - Reliable event publishing

---

## 🚀 Quick Start

### Prerequisites

- **Mininum memory** - 8Gb
- **Java 17+** - For building the project
- **Node.js 22+** - For frontend development
- **Docker & Docker Compose** - For running services

### Backend Startup

For this style, we can easily use our simple wrapper scripts from the project root. They will invoke the relevant on-prem Docker compose scripts.

```bash
# Start services
./startup.sh

# Start services (skip builds)
./startup.sh --skip-build
```

### Frontend Startup

To start frontend, we need to make a `.env` file in `frontend` directory:

```
VITE_API_BASE_URL=http://localhost:8080
```

Then start Frontend in development mode:

```bash
cd frontend
npm install
npm run dev
```

The web application will be accessible at **http://localhost:9000**.

Login Credentials:
- **Customer Portal**: `user` / `Aa@123456`
- **Admin Dashboard**: `admin` / `Aa@123456`

---

## On-Premise deployments

### 🐳 Docker Compose deployment

```bash
cd deployment/onprem/compose/scripts

# Start services
./compose-start.sh

# Start services (skip builds)
./compose-start.sh --skip-build

# Stop services
./compose-stop.sh

# Clean up resources
./compose-clean.sh
```

### ☸️ Kubernetes deployment

Atlas provides a simplified, kubectl-based deployment for Kubernetes with full environment support. Each environment runs in a separate namespace.

**Prerequisites:**
- Kubernetes cluster (minikube, kind, Docker Desktop, etc.)
- kubectl configured and connected to your cluster
- [Lens IDE](https://k8slens.dev/) (optional but highly recommended for monitoring)

**Quick Start Scripts:**

Atlas provides streamlined scripts for Kubernetes deployment management. All scripts are located in `deployment/onprem/k8s/scripts/`.

```bash
# Navigate to Kubernetes deployment directory
cd deployment/onprem/k8s/scripts

# Start services
./k8s-start.sh                          # Local environment (default)
./k8s-start.sh --env local              # Specified environment

# Start services (skip builds)
./k8s-start.sh --skip-build             # Local environment (default)
./k8s-start.sh --env local --skip-build # Specified environment

# Stop services: scales to 0 replicas, preserves all data
./k8s-stop.sh                           # Local environment (default)
./k8s-stop.sh --env local               # Specified environment

# Clean up resources
./k8s-clean.sh                          # Local environment (default)
./k8s-clean.sh --env local              # Specified environment
```

The startup process may take around **15 minutes** to complete (include building).

**Access Services via Ingress (Recommended):**

After setting up Ingress, access services using local hostnames:
- **API Gateway**: http://api.atlas.local
- **Grafana**: http://grafana.atlas.local (admin/admin)
- **Prometheus**: http://prometheus.atlas.local
- **Zipkin**: http://zipkin.atlas.local
- **SMTP4Dev**: http://mail.atlas.local

**Alternative: Access Services (Port Forwarding):**

```bash
# API Gateway
kubectl port-forward -n atlas-local svc/api-gateway 8080:8080

# Grafana
kubectl port-forward -n atlas-local svc/grafana 3000:3000

# Prometheus
kubectl port-forward -n atlas-local svc/prometheus 9090:9090

# Zipkin
kubectl port-forward -n atlas-local svc/zipkin 9411:9411
```

**Monitoring with Lens IDE**

**[Lens](https://k8slens.dev/)** is the most popular Kubernetes IDE with over 1 million users worldwide. It provides an intuitive, context-aware UI for managing and troubleshooting Atlas workloads.

Installation:
1. Download Lens from [k8slens.dev](https://k8slens.dev/)
2. Install and launch the application
3. Connect to your cluster (automatically detects your kubectl context)

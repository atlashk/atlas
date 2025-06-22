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

Atlas uses a unified DevOps approach with reorganized build scripts. Choose your preferred deployment method below.

**Deployment Options:**
- **Docker Compose** - Recommended for local development, includes Eureka service discovery
- **Kubernetes** - Production-ready with native DNS service discovery, no Eureka required

### Prerequisites

- **Mininum memory** - 8Gb
- **Java 17+** - For building the project
- **Node.js 22+** - For frontend development
- **Docker & Docker Compose** - For running services
- **kubectl** (optional) - For Kubernetes deployment

### ⚡ Super Quick Start (Recommended)

For first-time setup, use our simple wrapper scripts from the project root:

```bash
# One-command setup: checks prerequisites, builds, and starts everything
./start.sh

# Quick restart (skip builds if images already exist)
./start.sh --skip-build

# Show help and options
./start.sh --help

# Stop all services gracefully
./stop.sh

# Clean up everything (containers, volumes, images)
./clean.sh

# Clean up with options
./clean.sh --containers-only  # Keep data volumes
./clean.sh --volumes-only     # Remove only volumes
./clean.sh --images-only      # Remove only Atlas images
```

**Start Script** (`./start.sh`) will:
- ✅ Check all prerequisites (Java 17+, Node.js 22+, Docker, etc.)
- ✅ Build backend JAR files (unless `--skip-build`)
- ✅ Build frontend (unless `--skip-build`)
- ✅ Build Docker images (unless `--skip-build`)
- ✅ Start all services (infrastructure + observability + microservices)
- ✅ Show you all the URLs and connection details

**Stop & Clean Scripts:**
- `./stop.sh` - Gracefully stops all running services
- `./clean.sh` - Removes containers, volumes, and images for complete cleanup

**Options:**
- `--skip-build` - Skip all build steps and use existing Docker images (much faster for restarts)
- `--help` - Show usage information for any script

### 🐳 Manual Docker Compose Setup

If you prefer manual control over each step:

```bash
# 1. Build backend JAR files
bash deployment/build/build-backend.sh --infra-stack="onprem-compose-observability" --skip-tests="true"

# 2. Build frontend
bash deployment/build/build-frontend.sh

# 3. Build Docker images
bash deployment/build/build-docker-images.sh all

# 4. Start all services
cd deployment/onprem/compose/scripts
bash compose-start.sh --skip-build

# 5. Check status
docker ps

# 6. View logs
docker logs <service-name>

# 7. Stop services
bash compose-stop.sh

# 8. Clean up everything
bash compose-clean.sh

# Or clean up with specific options
bash compose-clean.sh --containers-only  # Keep volumes/data
bash compose-clean.sh --volumes-only     # Remove only volumes
bash compose-clean.sh --images-only      # Remove only images
```

### ☸️ Kubernetes Setup

Atlas provides a simplified, kubectl-based deployment for Kubernetes with full environment support. Each environment runs in a separate namespace.

**Prerequisites:**
- Kubernetes cluster (minikube, kind, Docker Desktop, etc.)
- kubectl configured and connected to your cluster

**Quick Start:**
```bash
# Navigate to Kubernetes deployment directory
cd deployment/onprem/k8s

# Start local development environment (Ingress setup included automatically)
./scripts/k8s-start.sh

# Start other environments
./scripts/k8s-start.sh dev     # Development environment
./scripts/k8s-start.sh stg     # Staging environment
./scripts/k8s-start.sh prod    # Production environment

# Stop environment
./scripts/k8s-stop.sh

# Complete cleanup (including volumes)
./scripts/k8s-clean.sh
```

**What happens automatically:**
- ✅ **NGINX Ingress Controller** - Installed automatically if not present
- ✅ **Local Hostnames** - Added to `/etc/hosts` (you may be prompted for sudo password)
- ✅ **Direct URL Access** - No port-forwarding needed
- ✅ **Platform Detection** - Works with minikube, kind, Docker Desktop automatically

**Access Services via Ingress (Recommended):**
After setting up Ingress, access services using local hostnames:
- **Frontend**: http://atlas.local
- **API Gateway**: http://api.atlas.local  
- **Grafana**: http://grafana.atlas.local (admin/admin)
- **Prometheus**: http://prometheus.atlas.local
- **Zipkin**: http://zipkin.atlas.local
- **SMTP4Dev**: http://mail.atlas.local

**Alternative: Access Services (Port Forwarding):**
```bash
# API Gateway
kubectl port-forward -n atlas-local svc/api-gateway 8080:8080

# Frontend
kubectl port-forward -n atlas-local svc/frontend 9000:9000

# Grafana
kubectl port-forward -n atlas-local svc/grafana 3000:3000

# Prometheus
kubectl port-forward -n atlas-local svc/prometheus 9090:9090

# Zipkin
kubectl port-forward -n atlas-local svc/zipkin 9411:9411
```

**Monitoring & Debugging:**
```bash
# Check deployment status
kubectl get all -n atlas-local

# View logs
kubectl logs -n atlas-local deployment/user-service -f

# Check ConfigMaps
kubectl get configmaps -n atlas-local

# Describe problematic pods
kubectl describe pod -n atlas-local <pod-name>
```

### 🌐 Access Frontend

The web application will be accessible at **http://localhost:9000**

**Login Credentials:**
- **Customer Portal**: `user` / `Aa@123456`
- **Admin Dashboard**: `admin` / `Aa@123456`

**Development Mode (Optional):**

If you want to run frontend in development mode:

```bash
cd frontend
npm install
npm run dev
# Access at http://localhost:5173
```

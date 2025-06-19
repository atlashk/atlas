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

### Infrastructure & DevOps
- **Docker & Docker Compose** - Containerization
- **Kubernetes** - Container orchestration (optional)
- **Eureka** - Service discovery
- **Spring Cloud Gateway** - API gateway
- **Zipkin** - Distributed tracing
- **Prometheus & Grafana** - Monitoring and metrics

### Architecture Patterns
- **Microservices Architecture** - Service decomposition
- **Domain-Driven Design (DDD)** - Business logic organization
- **Event-Driven Architecture** - Asynchronous communication
- **CQRS** - Command Query Responsibility Segregation
- **Outbox Pattern** - Reliable event publishing

---

## 🚀 Quick Start

Atlas uses a unified DevOps approach with reorganized build scripts. Choose your preferred deployment method below.

### Prerequisites

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

**Note:** When using manual setup, you work directly with the compose scripts in `deployment/onprem/compose/scripts/`:
- `compose-start.sh` - Start all services
- `compose-stop.sh` - Stop all services gracefully  
- `compose-clean.sh` - Clean up containers, volumes, and images

### ☸️ Kubernetes Setup

### 🌐 Access Frontend

The frontend is automatically built and deployed as part of the Docker Compose stack.

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

### 📊 Service URLs

Once running, access these services:

| Service | URL | Credentials |
|---------|-----|-------------|
| **Frontend (Customer)** | http://localhost:9000 | user : Aa@123456 |
| **Frontend (Admin)** | http://localhost:9000/admin | admin : Aa@123456 |
| **API Gateway** | http://localhost:8080 | - |
| **Eureka Discovery** | http://localhost:8761 | - |
| **Prometheus** | http://localhost:9090 | - |
| **Grafana** | http://localhost:3000 | admin : admin |
| **Zipkin Tracing** | http://localhost:9411 | - |
| **SMTP4Dev (Email)** | http://localhost:5000 | - |

### 🗃️ Database Connections

| Database | Connection | Credentials |
|----------|------------|-------------|
| **MySQL** | localhost:3306 | root : root |
| **Redis** | localhost:6379 | (no password) |
| **Kafka** | localhost:9092 | (no auth) |

### 📚 Documentation

For detailed documentation, see the [wiki/](wiki/) directory which contains:
- Architecture documentation
- API design guidelines
- Deployment strategies
- Development best practices

### 🏗️ Project Structure

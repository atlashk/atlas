# Atlas

## Project Overview

**Atlas** is a microservices-based platform.

---

## Architecture

- **Microservices**: Modular architecture for scalability and flexibility.
- **Hexagonal Architecture**: Separation of concerns to enhance maintainability.
- **Cross-Cutting Concerns**: Centralized handling of concerns such as logging, security, and monitoring.

---

## Technical Stack

---

## 🚀 Quick Start

Atlas now uses a unified DevOps approach. Choose your preferred deployment method below.

### Prerequisites

- **Java 17+** - For building the project
- **Docker & Docker Compose** - For running services
- **kubectl** (optional) - For Kubernetes deployment

### ⚡ Super Quick Start (Recommended)

For first-time setup, run our automated setup script:

```bash
# One-command setup: installs, builds, and starts everything
bash devops/scripts/setup/setup-dev-env.sh
```

This script will:
- ✅ Check all prerequisites (Java, Docker, etc.)
- ✅ Build the project
- ✅ Set up local environment configuration
- ✅ Start all services (infrastructure + observability + backend)
- ✅ Show you all the URLs and connection details

### 🐳 Manual Docker Compose Setup

If you prefer manual control:

```bash
# 1. Build the project
bash devops/scripts/build/build-all.sh

# 2. Start all services
cd devops/onprem/compose/scripts
./deploy.sh local up

# 3. Check status
./deploy.sh local status

# 4. View logs
./deploy.sh local logs

# 5. Stop when done
./deploy.sh local down
```

### ☸️ Kubernetes Setup

For Kubernetes deployment:

```bash
# 1. Build the project
bash devops/scripts/build/build-all.sh

# 2. Start K8s deployment
cd devops/onprem/k8s/scripts
./deploy.sh local apply

# 3. Check status
./deploy.sh local status

# 4. Clean up when done
./deploy.sh local delete
```

### 🌐 Start Frontend

After backend services are running:

```bash
cd frontend
npm install
npm run dev
```

The web application will be accessible at **http://localhost:9000**

**Login Credentials:**
- **Front site**: `user` / `Aa@123456`
- **Admin site**: `admin` / `Aa@123456`

### 📊 Service URLs

Once running, access these services:

| Service | URL | Credentials |
|---------|-----|-------------|
| **Frontend** | http://localhost:9000 | user/admin : Aa@123456 |
| **RabbitMQ Management** | http://localhost:15672 | admin : admin123 |
| **Prometheus** | http://localhost:9090 | - |
| **Grafana** | http://localhost:3000 | - |
| **Zipkin Tracing** | http://localhost:9411 | - |

### 🗃️ Database Connections

| Database | Connection | Credentials |
|----------|------------|-------------|
| **MySQL** | localhost:3306 | atlas : atlas123 |
| **Redis** | localhost:6379 | password: redis123 |

### 🧹 Cleanup & Management

```bash
# View service status
devops/onprem/compose/scripts/deploy.sh local status

# View logs for specific service
devops/onprem/compose/scripts/deploy.sh local logs mysql

# Restart all services  
devops/onprem/compose/scripts/deploy.sh local restart

# Stop all services
devops/onprem/compose/scripts/deploy.sh local down
```

### 🔧 Different Environments

Atlas supports multiple environments:

```bash
# Local development (default)
./deploy.sh local up

# Development environment
./deploy.sh dev up

# Staging environment  
./deploy.sh stg up

# Production environment
./deploy.sh prod up
```

### 🆘 Troubleshooting

If you encounter issues:

1. **Port conflicts**: Check if ports 3306, 6379, 5672, 9090, 3000 are free
2. **Docker issues**: Ensure Docker is running: `docker ps`
3. **Build issues**: Try cleaning: `./gradlew clean build`
4. **Logs**: Check service logs: `./deploy.sh local logs [service-name]`

For detailed documentation, see [devops/README.md](devops/README.md)

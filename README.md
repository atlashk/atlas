# Atlas

## Project Overview

**Atlas** is a microservices-based platform.

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
bash scripts/start.sh

# Quick restart (skip builds if images already exist)
bash scripts/start.sh --skip-build

# Show help and options
bash scripts/start.sh --help
```

This script will:
- ✅ Check all prerequisites (Java, Docker, etc.)
- ✅ Build backend JAR files (unless `--skip-build`)
- ✅ Build frontend (unless `--skip-build`)
- ✅ Build Docker images (unless `--skip-build`)
- ✅ Start all services (infrastructure + observability + backend)
- ✅ Show you all the URLs and connection details

**Options:**
- `--skip-build` - Skip all build steps and use existing Docker images (much faster for restarts)
- `--help` - Show usage information

### 🐳 Manual Docker Compose Setup

If you prefer manual control:

```bash
# 1. Build backend JAR files
bash build/build-backend.sh --infra-stack="onprem-compose-observability" --skip-tests="true"

# 2. Build frontend
bash build/build-frontend.sh

# 3. Build Docker images
bash build/build-docker-images.sh all

# 4. Start all services
cd deployment/onprem/compose/scripts
bash compose-start.sh

# 5. Check status
docker ps

# 6. View logs
docker logs <service-name>

# 7. Stop when done
bash compose-clean.sh
```

### ☸️ Kubernetes Setup

For Kubernetes deployment:

```bash
# 1. Build the project
bash scripts/start.sh --skip-build  # Build images first

# 2. Start K8s deployment
cd deployment/onprem/k8s/scripts
bash deploy.sh

# 3. Check status
kubectl get pods

# 4. Clean up when done
kubectl delete -f ../base/
```

### 🌐 Access Frontend

The frontend is automatically built and deployed as part of the Docker Compose stack.

The web application will be accessible at **http://localhost:80**

**Login Credentials:**
- **Front site**: `user` / `Aa@123456`
- **Admin site**: `admin` / `Aa@123456`

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
| **Frontend** | http://localhost:80 | user/admin : Aa@123456 |
| **API Gateway** | http://localhost:8080 | - |
| **Prometheus** | http://localhost:9090 | - |
| **Grafana** | http://localhost:3000 | admin : admin |
| **Zipkin Tracing** | http://localhost:9411 | - |
| **SMTP4Dev (Email)** | http://localhost:5000 | - |

### 🗃️ Database Connections

| Database | Connection | Credentials |
|----------|------------|-------------|
| **MySQL** | localhost:3306 | root : root |
| **Redis** | localhost:6379 | (no password) |

### 🧹 Cleanup & Management

```bash
# View service status
docker ps

# View logs for specific service
docker logs <service-name>
# Example: docker logs eureka-server

# Restart specific service
docker restart <service-name>

# Stop and clean up everything
cd deployment/onprem/compose/scripts
bash compose-clean.sh

# Clean up only containers (keep volumes/data)
bash compose-clean.sh --containers-only

# Clean up only volumes
bash compose-clean.sh --volumes-only

# Clean up only images
bash compose-clean.sh --images-only
```

### 🆘 Troubleshooting

If you encounter issues:

1. **Port conflicts**: Check if ports 80, 3000, 3306, 6379, 8080, 8761, 9090, 9411 are free
2. **Docker issues**: Ensure Docker is running: `docker ps`
3. **Build issues**: Try rebuilding: `bash scripts/start.sh` (without --skip-build)
4. **Health check failures**: Check service logs: `docker logs <service-name>`
5. **Clean start**: Use `bash deployment/onprem/compose/scripts/compose-clean.sh` then restart

**Common Solutions:**
- **Eureka server unhealthy**: Wait 60-90 seconds for full startup
- **Database connection errors**: Ensure MySQL container is healthy
- **Frontend not loading**: Check if API Gateway is running on port 8080
- **Out of disk space**: Clean up with `docker system prune -f`

For detailed documentation, see [wiki/](wiki/) directory

# Atlas - Docker Compose Deployment

This directory contains Docker Compose deployment configuration for Atlas microservices platform.

The Docker Compose setup includes:
- **Microservices**: User, Product, Order, Notification applications
- **Infrastructure**: API Gateway, Auth Server, Config Server, Discovery Server
- **Databases**: MySQL, Redis
- **Messaging**: Apache Kafka
- **Observability**: Prometheus, Grafana, Zipkin, Loki, Promtail
- **Development Tools**: SMTP4Dev for email testing

---

## Prerequisites

- **Minimum memory** - 8GB
- **Java 17+** - For building the project
- **Docker & Docker Compose** - For running services

---

## Quick Start

Navigate to this directory and use the deployment scripts:

```bash
cd backend/scripts/deploy/onprem/compose

# Start services
./deploy.sh

# Start services (skip builds)
./deploy.sh --skip-build

# Stop services
./stop.sh

# Clean up resources
./cleanup.sh
```

---

## Access Methods

### Option 1: Direct Access (No Nginx)
- **API Gateway**: http://localhost:8080
- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090
- **Zipkin**: http://localhost:9411
- **SMTP4Dev**: http://localhost:5000

### Option 2: Access via Nginx

This option requires hosts file configuration:

**For Windows:**
Edit `C:\Windows\System32\drivers\etc\hosts` as Administrator and add:
```
127.0.0.1 api.atlas.local
127.0.0.1 grafana.atlas.local
127.0.0.1 prometheus.atlas.local
127.0.0.1 zipkin.atlas.local
127.0.0.1 smtp4dev.atlas.local
```

**For macOS/Linux:**
Edit `/etc/hosts` with sudo and add:
```
127.0.0.1 api.atlas.local
127.0.0.1 grafana.atlas.local
127.0.0.1 prometheus.atlas.local
127.0.0.1 zipkin.atlas.local
127.0.0.1 smtp4dev.atlas.local
```

Then, we will be able to access services using local hostnames:
- **API Gateway**: http://api.atlas.local
- **Grafana**: http://grafana.atlas.local (admin/admin)
- **Prometheus**: http://prometheus.atlas.local
- **Zipkin**: http://zipkin.atlas.local
- **SMTP4Dev**: http://smtp4dev.atlas.local

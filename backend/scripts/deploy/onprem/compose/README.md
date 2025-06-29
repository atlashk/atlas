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

## Access Points

After deployment, services will be available at:

- **API Gateway**: http://localhost:8080
- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090
- **Zipkin**: http://localhost:9411
- **SMTP4Dev**: http://localhost:5000

---

## Troubleshooting

- Ensure Docker has sufficient memory allocated (minimum 8GB)
- Check that required ports are not in use by other applications
- Use `docker-compose logs <service-name>` to debug specific service issues
- Use `./cleanup.sh` to completely reset the environment if needed

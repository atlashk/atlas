# Atlas Microservices - Docker Compose Deployment

This directory contains the unified Docker Compose configuration for deploying the Atlas microservices platform locally using Docker Compose.

## Overview

The Atlas platform has been consolidated into a single, unified `docker-compose.yml` file that contains all services organized by category:

- **Infrastructure Services**: MySQL, Redis, Kafka, SMTP4Dev
- **Backend Services**: Discovery Server, Auth Server, API Gateway, User Service, Product Service, Order Service, Notification Service
- **Observability Services**: Loki, Promtail, Prometheus, Zipkin, Grafana
- **Frontend Services**: Web Application

## Quick Start

### Prerequisites

- Docker and Docker Compose installed
- Java 17+ (for building backend services)
- Node.js 22+ (for building frontend)

### Build and Start All Services

```bash
# From project root
./scripts/start.sh
```

This will:
1. Build all backend JAR files
2. Build the frontend
3. Build all Docker images
4. Start all services using the unified compose file

### Manual Service Management

You can also manage services manually using the compose scripts:

```bash
cd deployment/onprem/compose/scripts

# Start all services
./compose-start.sh

# Stop all services
./compose-stop.sh

# Clear all volumes (removes all data)
./compose-clear.sh
```

### Direct Docker Compose Commands

```bash
cd deployment/onprem/compose

# Start all services
docker-compose up -d

# Start specific services
docker-compose up -d mysql redis discovery-server api-gateway frontend

# View logs
docker-compose logs -f

# Check service status
docker-compose ps

# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

## Health Checks

The unified compose file includes comprehensive health checks for all services to ensure reliable startup and monitoring.

### Health Check Features

- **Automatic Dependency Management**: Services only start when their dependencies are healthy
- **Fast Failure Detection**: Unhealthy services detected within 10 seconds
- **Proper Startup Timing**: Grace periods configured for each service type
- **Monitoring Integration**: Health status visible via Docker Compose commands

### Health Check Commands

```bash
# Check service health status
docker-compose ps

# View health check logs for a specific service
docker-compose logs [service-name] | grep -i health

# Restart unhealthy services
docker-compose restart [service-name]
```

### Health Check Configuration

Each service has tailored health checks:

- **Infrastructure Services**: Database connectivity, cache availability, message broker readiness
- **Backend Services**: Spring Boot Actuator health endpoints
- **Observability Services**: Service-specific health endpoints
- **Frontend Services**: HTTP availability checks

Health check timing:
- **Interval**: 10s (regular checks)
- **Timeout**: 3-10s (based on service complexity)
- **Retries**: 5 attempts before marking unhealthy
- **Start Period**: 10s-120s (grace period for initialization)

## Service URLs

Once started, services are available at:

| Service | URL | Description |
|---------|-----|-------------|
| Frontend | http://localhost:80 | Main web application |
| API Gateway | http://localhost:8080 | Main API entry point |
| Discovery Server | http://localhost:8761 | Eureka service registry |
| Auth Server | http://localhost:8091 | Authentication service |
| User Service | http://localhost:8081 | User management |
| Product Service | http://localhost:8082 | Product catalog |
| Order Service | http://localhost:8083 | Order management |
| Notification Service | http://localhost:8084 | Notifications |
| MySQL | localhost:3306 | Database |
| Redis | localhost:6379 | Cache |
| Kafka | localhost:9092 | Message broker |
| SMTP4Dev | http://localhost:5000 | Email testing |
| Prometheus | http://localhost:9090 | Metrics |
| Grafana | http://localhost:3000 | Dashboards (admin/admin) |
| Zipkin | http://localhost:9411 | Distributed tracing |
| Loki | http://localhost:3100 | Log aggregation |

## Architecture

### Network Configuration

All services run on a single Docker network (`atlas-network`) enabling seamless communication between services across different categories.

### Service Dependencies

The compose file includes proper dependency management:

- **Infrastructure services** start first (MySQL, Redis, Kafka)
- **Backend services** depend on infrastructure and discovery server
- **Frontend** depends on API Gateway
- **Observability services** can start independently

### Volume Management

Persistent volumes are used for:
- Database data (MySQL)
- Cache data (Redis)
- Message broker data (Kafka)
- Application logs (all services)
- Monitoring data (Grafana)

## Configuration Files

- `docker-compose.yml` - Unified compose file with all services
- `configs/` - Configuration files for various services
  - `mysql/` - Database initialization scripts
  - `prometheus/` - Prometheus configuration
  - `promtail/` - Log collection configuration

## Legacy Files

The following files are kept for reference but are no longer used:
- `docker-compose.backend.yml`
- `docker-compose.frontend.yml`
- `docker-compose.infrastructure.yml`
- `docker-compose.observability.yml`

## Troubleshooting

### Common Issues

1. **Port conflicts**: Ensure no other services are using the required ports
2. **Memory issues**: Increase Docker memory limits if services fail to start
3. **Network issues**: Restart Docker if network creation fails

### Debugging

```bash
# View service logs
docker-compose logs -f [service-name]

# Check service health
docker-compose ps

# Inspect network
docker network inspect atlas-network

# Check volumes
docker volume ls | grep atlas-onprem-compose
```

### Clean Restart

```bash
# Stop all services and remove volumes
./compose-stop.sh
./compose-clear.sh

# Remove any orphaned containers
docker container prune -f

# Start fresh
./compose-start.sh
```

## Development Workflow

The unified approach starts all services in the proper dependency order:

1. **Infrastructure services** (MySQL, Redis, Kafka, SMTP4Dev)
2. **Observability services** (Loki, Promtail, Prometheus, Zipkin, Grafana)
3. **Backend services** (Discovery Server, Auth Server, API Gateway, microservices)
4. **Frontend services** (Web Application)

All services are started with a single command and proper health checks ensure dependencies are ready before dependent services start. 
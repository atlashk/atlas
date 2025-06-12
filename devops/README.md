# Atlas DevOps

This directory contains **ALL** DevOps-related configurations and scripts for the Atlas project. This unified approach eliminates the previous separation between `deployment/` and `scripts/` directories, providing a single source of truth for all DevOps operations.

## 🏗️ Structure Overview

```
devops/
├── onprem/                      # On-premises deployments
│   ├── compose/                 # Docker Compose deployments
│   │   ├── configs/             # Shared compose configurations
│   │   ├── environments/        # Environment-specific variables
│   │   └── scripts/             # Compose deployment scripts
│   └── k8s/                     # Kubernetes deployments
│       ├── base/                # Base K8s configurations
│       ├── environments/        # Environment overlays
│       └── scripts/             # K8s deployment scripts
├── scripts/                     # All automation scripts
│   ├── build/                   # Build automation
│   ├── lib/                     # Script libraries (logger, common)
│   └── setup/                   # Initial project setup scripts
├── helm/                        # Helm charts (alternative to Kustomize)
├── README.md                    # This file
└── MIGRATION.md                 # Migration guide
```

## 🚀 Quick Start

### ⚡ First-Time Setup

For initial project setup (recommended for new developers):

```bash
# Automated setup: checks prerequisites, builds, and starts everything
bash devops/scripts/setup/setup-dev-env.sh
```

### 🐳 Docker Compose Deployment

```bash
# Deploy to local environment
cd devops/onprem/compose/scripts
./deploy.sh local up

# Deploy to development environment
./deploy.sh dev up

# Check status
./deploy.sh local status

# View logs
./deploy.sh local logs

# Stop services
./deploy.sh local down
```

### Kubernetes Deployment

```bash
# Deploy to local environment
cd devops/onprem/k8s/scripts
./deploy.sh local apply

# Deploy to staging environment
./deploy.sh stg apply

# Check status
./deploy.sh local status

# View logs
./deploy.sh local logs

# Delete all resources
./deploy.sh local delete
```

## 📁 Detailed Structure

### Docker Compose

#### Configs Directory
Contains the base Docker Compose files that are shared across environments:
- `docker-compose.infra.yml` - Infrastructure services (MySQL, Redis, RabbitMQ)
- `docker-compose.observability.yml` - Monitoring stack (Prometheus, Grafana, Zipkin)
- `docker-compose.backend.yml` - Application services
- Service-specific configuration directories (mysql/, rabbitmq/, prometheus/, promtail/)

#### Environments Directory
Environment-specific configuration files:
- `env.local` - Local development settings
- `env.dev` - Development environment settings
- `env.stg` - Staging environment settings (uses environment variables for secrets)
- `env.prod` - Production environment settings (uses environment variables for secrets)

#### Scripts Directory
Deployment and management scripts:
- `deploy.sh` - Main deployment script with environment support
- `compose-up.sh` - Legacy compose up script
- `compose-down.sh` - Legacy compose down script

### Kubernetes

#### Base Directory
Contains the base Kubernetes configurations using Kustomize:
- `infrastructure/` - Infrastructure components (databases, caches, messaging)
- `microservices/` - Application microservices
- `observability/` - Monitoring and logging components
- `kustomization.yaml` - Base kustomization configuration

#### Environments Directory
Environment-specific Kustomize overlays:
- `local/` - Local development overlays (minikube, kind)
- `dev/` - Development environment overlays
- `stg/` - Staging environment overlays
- `prod/` - Production environment overlays

#### Scripts Directory
Kubernetes deployment and management scripts:
- `deploy.sh` - Main deployment script with environment support

## 🔧 Environment Configuration

### Environment Variables for Staging/Production

For staging and production environments, sensitive values should be set as environment variables:

```bash
# Staging environment
export MYSQL_ROOT_PASSWORD_STG="your-secure-password"
export MYSQL_PASSWORD_STG="your-secure-password"
export REDIS_PASSWORD_STG="your-secure-password"
export RABBITMQ_PASSWORD_STG="your-secure-password"

# Production environment
export MYSQL_ROOT_PASSWORD_PROD="your-secure-password"
export MYSQL_PASSWORD_PROD="your-secure-password"
export REDIS_PASSWORD_PROD="your-secure-password"
export RABBITMQ_PASSWORD_PROD="your-secure-password"
```

### Environment Characteristics

| Environment | Purpose | Resource Allocation | Security | Persistence |
|-------------|---------|-------------------|----------|-------------|
| **local** | Development | Minimal (512MB-1GB) | Basic auth | Temporary |
| **dev** | Integration Testing | Medium (1-2GB) | Basic auth | Temporary |
| **stg** | Pre-production Testing | High (2-4GB) | Secure (env vars) | Persistent |
| **prod** | Production | High (4GB+) | Secure + TLS | Persistent + Backup |

## 📋 Available Commands

### Docker Compose Scripts

```bash
# Deploy services
./deploy.sh [environment] up
./deploy.sh [environment] down
./deploy.sh [environment] restart

# Monitoring
./deploy.sh [environment] status
./deploy.sh [environment] logs [service-name]

# Environments: local, dev, stg, prod
```

### Kubernetes Scripts

```bash
# Deploy services
./deploy.sh [environment] apply
./deploy.sh [environment] delete
./deploy.sh [environment] restart

# Monitoring
./deploy.sh [environment] status
./deploy.sh [environment] logs [service-name]

# Environments: local, dev, stg, prod
```

## 🔍 Monitoring and Observability

### Exposed Ports (Local Environment)

| Service | Port | Description |
|---------|------|-------------|
| MySQL | 3306 | Database |
| Redis | 6379 | Cache |
| RabbitMQ | 5672 | Message Queue |
| RabbitMQ Management | 15672 | Web UI |
| Kafka | 9092 | Event Streaming |
| Prometheus | 9090 | Metrics Collection |
| Grafana | 3000 | Dashboards |
| Zipkin | 9411 | Distributed Tracing |

### Access URLs

- **RabbitMQ Management**: http://localhost:15672 (admin/admin123)
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000
- **Zipkin**: http://localhost:9411

## 🛠️ Development Workflow

1. **Local Development**
   ```bash
   cd devops/onprem/compose/scripts
   ./deploy.sh local up
   ```

2. **Integration Testing**
   ```bash
   ./deploy.sh dev up
   ```

3. **Staging Deployment**
   ```bash
   # Set environment variables first
   ./deploy.sh stg up
   ```

4. **Production Deployment**
   ```bash
   # Set environment variables first
   ./deploy.sh prod up
   ```

## 🔐 Security Notes

- Local and dev environments use hardcoded passwords for convenience
- Staging and production use environment variables for all sensitive data
- Production environment includes TLS configuration
- All production secrets should be managed through external secret management systems

## 🐛 Troubleshooting

### Common Issues

1. **Port conflicts**: Check if ports are already in use
2. **Permission denied**: Ensure scripts are executable
3. **Environment variables**: Verify all required variables are set for stg/prod
4. **Docker/Kubernetes**: Ensure Docker or Kubernetes is running

### Debug Commands

```bash
# Check service logs
./deploy.sh [env] logs [service]

# Check service status
./deploy.sh [env] status

# For Kubernetes, check individual pods
kubectl get pods -n atlas-[env]
kubectl describe pod [pod-name] -n atlas-[env]
```

## 📈 Future Enhancements

- [ ] Add AWS EKS configurations
- [ ] Add GCP GKE configurations
- [ ] Implement GitOps with ArgoCD
- [ ] Add automated backup scripts
- [ ] Implement blue-green deployment strategies
- [ ] Add performance testing configurations 
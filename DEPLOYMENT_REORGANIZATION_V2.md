# Deployment & Scripts Reorganization Plan v2

## Overview
Reorganize deployment and scripts following industry best practices for multi-environment, multi-platform deployments.

## Proposed Structure

```
deployment/                           # Infrastructure as Code (IaC)
├── base/                            # Base Kustomize configurations
│   ├── infrastructure/              # Shared infrastructure components
│   │   ├── mysql.yaml
│   │   ├── kafka.yaml
│   │   ├── redis.yaml
│   │   ├── rabbitmq.yaml
│   │   ├── smtp4dev.yaml
│   │   └── kustomization.yaml
│   ├── microservices/               # Shared microservice configurations
│   │   ├── user-service.yaml
│   │   ├── product-service.yaml
│   │   ├── order-service.yaml
│   │   ├── notification-service.yaml
│   │   ├── api-gateway.yaml
│   │   ├── auth-server.yaml
│   │   └── kustomization.yaml
│   └── observability/               # Monitoring and logging
│       ├── prometheus.yaml
│       ├── grafana.yaml
│       ├── zipkin.yaml
│       └── kustomization.yaml
├── environments/
│   ├── local/                       # Local development
│   │   ├── compose/                 # Docker Compose for local dev
│   │   │   ├── docker-compose.yml
│   │   │   ├── docker-compose.override.yml
│   │   │   ├── .env.local
│   │   │   └── volumes/
│   │   └── k8s/                     # K8s for local testing
│   │       ├── kustomization.yaml
│   │       └── patches/
│   ├── dev/                         # Development environment
│   │   ├── compose/                 # Compose-based dev environment
│   │   │   ├── docker-compose.yml
│   │   │   ├── .env.dev
│   │   │   └── volumes/
│   │   └── k8s/                     # K8s-based dev environment
│   │       ├── kustomization.yaml
│   │       ├── patches/
│   │       └── secrets/
│   ├── staging/                     # Staging environment
│   │   ├── onprem/
│   │   │   └── k8s/
│   │   │       ├── kustomization.yaml
│   │   │       ├── patches/
│   │   │       └── secrets/
│   │   ├── aws/
│   │   │   ├── eks/
│   │   │   │   ├── kustomization.yaml
│   │   │   │   └── patches/
│   │   │   └── terraform/
│   │   │       ├── main.tf
│   │   │       ├── variables.tf
│   │   │       └── terraform.tfvars
│   │   └── gcp/
│   │       ├── gke/
│   │       │   ├── kustomization.yaml
│   │       │   └── patches/
│   │       └── terraform/
│   └── production/                  # Production environment
│       ├── onprem/
│       │   └── k8s/
│       │       ├── kustomization.yaml
│       │       ├── patches/
│       │       └── secrets/
│       ├── aws/
│       │   ├── eks/
│       │   │   ├── kustomization.yaml
│       │   │   └── patches/
│       │   └── terraform/
│       │       ├── main.tf
│       │       ├── variables.tf
│       │       └── terraform.tfvars
│       └── gcp/
│           ├── gke/
│           │   ├── kustomization.yaml
│           │   └── patches/
│           └── terraform/
└── helm/                            # Helm charts (if needed)
    ├── atlas-infrastructure/
    └── atlas-microservices/

scripts/                             # Automation and tooling
├── build/                           # Build automation
│   ├── build-jars.sh               # Build Java applications
│   ├── build-images.sh             # Build Docker images
│   ├── build-all.sh                # Build everything
│   └── clean.sh                    # Clean build artifacts
├── deploy/                          # Deployment automation
│   ├── local/
│   │   ├── compose-up.sh           # Start local compose
│   │   ├── compose-down.sh         # Stop local compose
│   │   └── k8s-local.sh            # Deploy to local k8s
│   ├── dev/
│   │   ├── deploy-compose.sh       # Deploy to dev compose
│   │   └── deploy-k8s.sh           # Deploy to dev k8s
│   ├── staging/
│   │   ├── deploy-onprem.sh        # Deploy to staging on-prem
│   │   ├── deploy-aws.sh           # Deploy to staging AWS
│   │   └── deploy-gcp.sh           # Deploy to staging GCP
│   ├── production/
│   │   ├── deploy-onprem.sh        # Deploy to prod on-prem
│   │   ├── deploy-aws.sh           # Deploy to prod AWS
│   │   └── deploy-gcp.sh           # Deploy to prod GCP
│   └── common/
│       ├── deploy-functions.sh     # Shared deployment functions
│       └── validate-deployment.sh  # Post-deployment validation
├── ops/                             # Operations scripts
│   ├── backup/
│   │   ├── backup-mysql.sh
│   │   └── backup-volumes.sh
│   ├── monitoring/
│   │   ├── health-check.sh
│   │   └── log-aggregation.sh
│   ├── maintenance/
│   │   ├── rolling-restart.sh
│   │   └── scale-services.sh
│   └── troubleshooting/
│       ├── debug-pods.sh
│       └── collect-logs.sh
├── dev/                             # Development utilities
│   ├── setup-dev-env.sh            # Setup development environment
│   ├── reset-data.sh               # Reset development data
│   ├── port-forward.sh             # Port forwarding for services
│   ├── generate-test-data.sh       # Generate test data
│   └── run-integration-tests.sh    # Run integration tests
├── lib/                             # Shared libraries and functions
│   ├── common.sh                   # Common functions
│   ├── logger.sh                   # Logging utilities
│   ├── config.sh                   # Configuration management
│   └── validation.sh               # Validation functions
└── ci/                              # CI/CD specific scripts
    ├── pipeline-build.sh           # CI build script
    ├── pipeline-test.sh            # CI test script
    ├── pipeline-deploy.sh          # CI deploy script
    └── quality-gates.sh            # Quality gate checks
```

## Migration Steps

### Phase 1: Restructure Environments
1. Create new environment-based structure
2. Move existing configurations to appropriate environments
3. Update Kustomize overlays for each environment

### Phase 2: Reorganize Scripts
1. Create new script structure
2. Move existing scripts to appropriate categories
3. Create shared library functions
4. Update script dependencies and paths

### Phase 3: Environment-Specific Configurations
1. Create environment-specific patches
2. Set up secrets management per environment
3. Configure resource limits per environment
4. Set up monitoring per environment

### Phase 4: CI/CD Integration
1. Update CI/CD pipelines to use new structure
2. Create environment promotion workflows
3. Set up automated testing per environment

## Benefits of This Structure

### 1. Clear Separation of Concerns
- **deployment/**: Declarative infrastructure configurations
- **scripts/**: Imperative automation and tooling

### 2. Environment Isolation
- Each environment has its own configuration
- Easy to promote changes through environments
- Environment-specific secrets and patches

### 3. Platform Flexibility
- Support for multiple deployment targets (on-prem, AWS, GCP)
- Consistent structure across platforms
- Easy to add new platforms

### 4. Developer Experience
- Clear local development setup
- Easy environment switching
- Comprehensive tooling for common tasks

### 5. Operations Support
- Dedicated operations scripts
- Monitoring and troubleshooting tools
- Backup and maintenance automation

## Configuration Management

### Environment Variables
```bash
# Environment-specific configuration
ENVIRONMENT=local|dev|staging|production
PLATFORM=onprem|aws|gcp
DEPLOYMENT_TYPE=compose|k8s
```

### Secrets Management
- Local: `.env` files (not committed)
- Dev/Staging: Kubernetes secrets or external secret management
- Production: External secret management (AWS Secrets Manager, GCP Secret Manager)

### Resource Configuration
- Local: Minimal resources for development
- Dev: Small but realistic resources
- Staging: Production-like resources
- Production: Full production resources

## Next Steps
1. Review and approve this structure
2. Create migration scripts
3. Update documentation
4. Train team on new structure
5. Implement CI/CD pipeline updates 
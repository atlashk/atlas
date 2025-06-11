# Deployment & Scripts Reorganization Plan

## Current Issues
1. **Scattered Scripts**: Build scripts separated from deployment scripts
2. **Inconsistent Paths**: Wrong path references in Docker Compose scripts
3. **Mixed Responsibilities**: Scripts handling multiple deployment targets
4. **Duplicate Functionality**: Similar utilities across different locations

## Proposed New Structure

```
├── scripts/                            # All scripts centralized
│   ├── build/                          # Build-related scripts
│   │   ├── build-jars.sh              # Build JAR files
│   │   ├── build-images.sh            # Build Docker images
│   │   └── build-all.sh               # Build everything
│   ├── deploy/                         # Deployment scripts
│   │   ├── onprem/                    # On-premise deployment scripts
│   │   │   ├── compose-deploy.sh      # Docker Compose deployment
│   │   │   ├── compose-cleanup.sh     # Docker Compose cleanup
│   │   │   ├── k8s-deploy.sh          # Kubernetes deployment
│   │   │   ├── k8s-cleanup.sh         # Kubernetes cleanup
│   │   │   └── minikube-setup.sh      # Minikube setup helper
│   │   └── aws/                       # AWS deployment scripts
│   │       ├── eks-deploy.sh          # EKS deployment
│   │       ├── eks-cleanup.sh         # EKS cleanup
│   │       └── infrastructure-setup.sh # AWS infrastructure setup
│   ├── util/                          # Shared utilities
│   │   ├── logger.sh                  # Logging functions
│   │   ├── common.sh                  # Common functions
│   │   ├── health-check.sh            # Health check utilities
│   │   └── image-utils.sh             # Image management utilities
│   └── dev/                           # Development utilities
│       ├── setup-dev-env.sh           # Development environment setup
│       ├── reset-data.sh              # Reset development data
│       └── port-forward.sh            # Port forwarding helper
├── deployment/                         # Deployment configurations only
│   ├── onprem/                        # On-premise deployment
│   │   ├── compose/                   # Docker Compose files
│   │   │   ├── docker-compose.yml     # Main compose file
│   │   │   ├── docker-compose.override.yml  # Local overrides
│   │   │   └── configs/               # Configuration files
│   │   └── k8s/                       # Kubernetes manifests
│   │       ├── base/                  # Kustomize base (current structure)
│   │       └── overlays/              # Environment overlays
│   │           ├── local/             # Local development
│   │           ├── dev/               # Development environment
│   │           ├── staging/           # Staging environment
│   │           └── production/        # Production environment
│   └── aws/                           # AWS deployment
│       ├── terraform/                 # Infrastructure as Code
│       ├── helm/                      # Helm charts
│       └── eks/                       # EKS-specific configurations
└── docs/                              # Documentation
    ├── deployment/                    # Deployment documentation
    │   ├── onprem-setup.md
    │   ├── kubernetes-deployment.md
    │   └── aws-deployment.md
    └── scripts/                       # Script documentation
        └── script-reference.md
```

## Migration Steps

### Phase 1: Reorganize Scripts
1. Create new `scripts/` directory structure
2. Move and rename existing scripts:
   - `script/build-*.sh` → `scripts/build/`
   - `deployment/onprem/k8s/scripts/` → `scripts/deploy/onprem/`
   - `deployment/onprem/compose/*.sh` → `scripts/deploy/onprem/`
   - `deployment/util/` → `scripts/util/`

### Phase 2: Consolidate Utilities
1. Create comprehensive utility scripts
2. Standardize logging across all scripts
3. Add common functions for image management, health checks

### Phase 3: Improve Deployment Configs
1. Keep `deployment/onprem/` structure (already correct)
2. Fix path references in Docker Compose scripts ✅ (already done)
3. Organize AWS deployment configurations

### Phase 4: Add Development Tools
1. Create development utility scripts
2. Add environment setup automation
3. Create data management scripts

## Benefits

1. **Centralized Scripts**: All scripts in one location
2. **Clear Separation**: Build vs Deploy vs Utilities
3. **Environment Specific**: Clear separation of local/k8s/cloud
4. **Reusable Utilities**: Shared functions across all scripts
5. **Better Documentation**: Clear structure for documentation
6. **Scalable**: Easy to add new deployment targets
7. **Developer Friendly**: Clear entry points for different tasks

## Implementation Priority

1. **High Priority**: Fix path issues in existing scripts
2. **Medium Priority**: Reorganize script structure
3. **Low Priority**: Add new utility scripts and documentation

## Backward Compatibility

- Keep symlinks to old script locations during transition
- Update CI/CD pipelines gradually
- Provide migration guide for developers 
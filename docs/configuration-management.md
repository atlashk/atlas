# Configuration Management

## Atlas notes

Configuration is driven by two layers:

1. **App-stack selection** (`backend/config/app-stack.*.yml`)
   - Chooses concrete adapters (datasource, messaging, storage, observability, IAM).
   - Feeds Gradle `appStack.*` properties for module wiring.
   - Feeds deployment generator to produce Compose or Kubernetes manifests.

2. **Runtime configuration** (environment variables)
   - Services read environment variables in `application.yml` defaults.
   - Examples include datasource, messaging, and IAM endpoints.

## Config server

The repository includes a Spring Cloud Config Server module under:

- `backend/platform/config-server/spring-cloud-config`

Enable it in deployments when you need centralized, externalized configuration across services.

## Practical guidance

- Keep secrets out of version control and inject through environment variables or secret stores
- Treat app-stack YAML as infrastructure topology, not as runtime secrets
- Regenerate deployment manifests after changing app-stack files

# Security

## Authentication

Atlas supports two IAM implementations selected by app-stack (`backend/config/app-stack.*.yml`):

- `iam: keycloak` uses Keycloak for user management and token issuance
- `iam: jwt` issues tokens directly using the internal JWT module

Gateway resource server configuration depends on the selected IAM module:

- JWT mode uses `jwk-set-uri` from IAM `/api/authentication/.well-known/jwks.json`
- Keycloak mode uses `issuer-uri` from the configured Keycloak realm

## Authorization

Role-based access control is enforced at the API Gateway:

- Admin routes apply `Authorization=ADMIN` filter
- User role is extracted from JWT claims

## API Gateway

Security policies are centralized in the gateway:

- Token validation for protected routes
- Role checks for admin routes
- Token relay to downstream services

## Secure Communication

- Prefer HTTPS between services in non-local environments
- Use service discovery + load balancing to avoid hardcoded service URLs

## Secret Management

- Do not commit secrets or client credentials
- Use environment variables for Keycloak admin credentials and client secrets
- Rotate secrets before enabling external access

# Atlas Microservices - AWS ECS Architecture

## Architecture Overview

```
Internet → ALB → API Gateway → AWS Cloud Map → Internal Services
                  (External)                   (All Internal)
```

## Services Classification

### External Services (ALB Access)
- **API Gateway** - Single entry point for all external traffic

### Internal Services (AWS Cloud Map Discovery Only)
- **Auth Server** - Authentication and authorization (accessed via `/api/auth/**`)
- **User Service** - User management (accessed via `/api/*/users/**`)
- **Product Service** - Product catalog (accessed via `/api/*/products/**`)
- **Order Service** - Order processing (accessed via `/api/*/orders/**`)
- **Notification Service** - Notifications (accessed via `/notification/**`)

## Key Architectural Corrections

### 1. Auth Server is Now Internal
- **Before**: Auth server had ALB listener rules (external access)
- **After**: Auth server is internal-only, accessed through API Gateway
- **Access Pattern**: `Internet → ALB → API Gateway → auth-server.atlas.{env}:8091`

### 2. AWS Cloud Map Service Discovery (No Eureka)
All internal services use AWS Cloud Map for service discovery:
- `user-service.atlas.{environment}:8081`
- `product-service.atlas.{environment}:8082`
- `order-service.atlas.{environment}:8083`
- `notification-service.atlas.{environment}:8084`

### 3. API Gateway Routes
Auth server is accessible through these routes:
- `/api/auth/login` (public)
- `/api/auth/logout` (authenticated)
- `/api/auth/**` (various auth endpoints)

## AWS Cloud Map Service Discovery

AWS ECS provides built-in service discovery through **AWS Cloud Map**, eliminating the need for Eureka server in cloud deployments.

### How AWS Cloud Map Works

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   API Gateway   │───▶│  AWS Cloud Map  │───▶│ Internal Service│
│                 │    │   DNS Queries   │    │   (ECS Tasks)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Implementation Details

#### 1. **Service Discovery Namespace**
```typescript
// Creates: atlas.{environment} (e.g., atlas.dev)
this.namespace = new servicediscovery.PrivateDnsNamespace(this, 'ServiceDiscoveryNamespace', {
  name: `atlas.${environmentName}`,
  vpc,
});
```

#### 2. **Service Registration**
```typescript
// Each service gets registered as: service-name.atlas.{environment}
const service = new servicediscovery.Service(this, 'ServiceDiscovery', {
  name: serviceName,  // e.g., 'user-service'
  namespace: this.namespace,
  dnsRecordType: servicediscovery.DnsRecordType.A,
});
```

#### 3. **ECS Service Association**
```typescript
// ECS automatically registers/deregisters task IPs
this.service.associateCloudMapService({
  service: cloudMapService,
});
```

### Service Discovery URLs

| Service | Cloud Map URL | Port |
|---------|---------------|------|
| User Service | `user-service.atlas.{env}` | 8081 |
| Product Service | `product-service.atlas.{env}` | 8082 |
| Order Service | `order-service.atlas.{env}` | 8083 |
| Notification Service | `notification-service.atlas.{env}` | 8084 |

## Benefits of AWS Cloud Map vs Eureka

### AWS Cloud Map Advantages:
1. **Managed Service**: No infrastructure to manage
2. **DNS-Based**: Works with any HTTP client
3. **Health Checks**: Integrated with ECS health checks
4. **Auto Registration**: ECS tasks automatically register/deregister
5. **Load Balancing**: Built-in DNS round-robin
6. **Cost**: No additional compute costs
7. **Reliability**: AWS-managed, highly available

### Eureka Disadvantages in AWS:
1. **Additional Infrastructure**: Need to run Eureka server
2. **Single Point of Failure**: Unless you run multiple instances
3. **Maintenance**: Updates, patches, monitoring
4. **Cost**: Additional ECS tasks/resources
5. **Complexity**: Extra service to manage

## Security Benefits

1. **No Direct Access**: Internal services cannot be accessed directly from internet
2. **Centralized Authentication**: All auth flows go through API Gateway
3. **Consistent Security**: Rate limiting, CORS, and security headers applied consistently
4. **Service Isolation**: Internal services are isolated in private subnets

## Service Communication Flow

```
1. Client → ALB → API Gateway
2. API Gateway → AWS Cloud Map → Auth Server (for authentication)
3. API Gateway → AWS Cloud Map → Business Services (for business logic)
4. Business Services operate independently (no direct auth server communication)
```

## API Gateway Configuration

The API Gateway uses Cloud Map URLs directly:

```yaml
# In API Gateway application.yml
routes:
  - id: user-service
    uri: ${USER_SERVICE_URI:lb://user-service}  # Spring Cloud Gateway load balancer
    # Resolves to: user-service.atlas.{env}:8081
```

## Spring Boot Configuration

For AWS deployment, Spring Cloud Discovery is not needed:

```typescript
// CDK configuration - no Eureka environment variables
environment: {
  MYSQL_URL: `jdbc:mysql://...`,
  REDIS_CLUSTER_NODES: `...`,
  // No EUREKA_DEFAULT_ZONE needed
}
```

### Service-to-Service Communication

Business services may communicate with each other when needed:

```java
// Example: Order service calling User service
@Value("${USER_SERVICE_URL:http://user-service.atlas.dev:8081}")
private String userServiceUrl;

// Example: Order service calling Product service
@Value("${PRODUCT_SERVICE_URL:http://product-service.atlas.dev:8082}")
private String productServiceUrl;

// Auth server operates independently - no direct calls to/from business services
```

## DNS Resolution Flow

1. **Service Call**: `http://user-service.atlas.dev:8081/api/users`
2. **DNS Query**: AWS Cloud Map resolves to healthy task IPs
3. **Load Balancing**: DNS returns different IPs for load distribution
4. **Health Checks**: Only healthy tasks are returned
5. **Auto Updates**: Task registration/deregistration is automatic

## Deployment Architecture

### Infrastructure Components

1. **VPC**: Private network with public/private subnets
2. **ALB**: Application Load Balancer for external access
3. **ECS Cluster**: Container orchestration platform
4. **AWS Cloud Map**: Service discovery namespace
5. **RDS**: MySQL database for persistent storage
6. **ElastiCache**: Redis for caching and sessions
7. **CloudWatch**: Logging and monitoring

### Service Deployment Pattern

```typescript
// External Service (API Gateway only)
export class ApiGatewayService extends Construct {
  // Creates ALB target group and listener rules
  // Deploys to private subnets (ALB handles external access)
}

// Internal Services (All business logic)
export class InternalService extends Construct {
  // No ALB target group or listener rules
  // Deploys to private subnets
  // Registers with AWS Cloud Map
}
```

## Comparison: On-Premises vs AWS

| Aspect | On-Premises (Docker Compose) | AWS ECS |
|--------|------------------------------|---------|
| Service Discovery | Eureka Server | AWS Cloud Map |
| Registration | Manual/Spring Cloud | Automatic (ECS) |
| Health Checks | Eureka health checks | ECS health checks |
| Load Balancing | Ribbon (client-side) | DNS round-robin |
| Infrastructure | Self-managed | AWS-managed |
| Scaling | Manual Eureka scaling | Auto-managed |
| External Access | Direct port exposure | ALB + API Gateway |

## Migration Notes

When migrating from on-premises to AWS:

1. **Remove Eureka Dependencies**: No need for `spring-cloud-starter-netflix-eureka-client`
2. **Update Service URLs**: Use Cloud Map DNS names instead of Eureka service IDs
3. **No Discovery Config**: Remove Eureka configuration from environment variables
4. **Health Checks**: Rely on ECS health checks instead of Eureka

## Monitoring and Troubleshooting

```bash
# Check Cloud Map services
aws servicediscovery list-services

# Check service instances
aws servicediscovery list-instances --service-id <service-id>

# DNS resolution test (from within VPC)
nslookup user-service.atlas.dev

# ECS service status
aws ecs describe-services --cluster atlas-cluster --services user-service
```

## Scaling and Performance

### Auto-Scaling Configuration
```typescript
// Can be added to any service
const autoScaling = service.autoScaleTaskCount({
  minCapacity: 1,
  maxCapacity: 20
});

autoScaling.scaleOnCpuUtilization('CpuScaling', {
  targetUtilizationPercent: 70
});

autoScaling.scaleOnMemoryUtilization('MemoryScaling', {
  targetUtilizationPercent: 80
});
```

### Load Balancing
- **External**: ALB distributes traffic to API Gateway instances
- **Internal**: AWS Cloud Map DNS round-robin for service-to-service calls
- **Database**: RDS handles connection pooling
- **Cache**: ElastiCache Redis cluster for distributed caching

## Conclusion

This architecture implements a proper microservices pattern with:

- **Single Entry Point**: Only API Gateway is externally accessible
- **Internal Services**: All business services are private and secure
- **AWS-Native Service Discovery**: No need for Eureka server overhead
- **Automatic Scaling**: ECS and AWS Cloud Map handle scaling automatically
- **Cost Effective**: Leverages AWS managed services
- **High Availability**: AWS-managed infrastructure with built-in redundancy

The combination of AWS ECS, Cloud Map, and proper API Gateway patterns provides a robust, scalable, and secure microservices architecture.

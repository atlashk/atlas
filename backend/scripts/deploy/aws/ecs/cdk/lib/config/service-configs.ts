import * as ecs from 'aws-cdk-lib/aws-ecs';
import { ServiceConfig } from '../constructs/base-service';
import { InfrastructureStack } from '../stack/infrastructure-stack';

export interface ServiceConfigFactory {
  createApiGatewayConfig(environmentName: string, infrastructure: InfrastructureStack): ServiceConfig;
  createAuthServerConfig(environmentName: string, infrastructure: InfrastructureStack): ServiceConfig;
}

export class DefaultServiceConfigFactory implements ServiceConfigFactory {

  createApiGatewayConfig(environmentName: string, infrastructure: InfrastructureStack): ServiceConfig {
    return {
      serviceName: 'api-gateway',
      containerPort: 8080,
      healthCheckPath: '/actuator/health',
      listenerPriority: 100,
      pathPatterns: ['/*'],
      environment: {
        // Service URIs for routing - using Cloud Map service discovery
        AUTH_SERVER_URI: `http://auth-server.atlas.${environmentName}:8091`,
        USER_SERVICE_URI: `http://user-service.atlas.${environmentName}:8081`,
        PRODUCT_SERVICE_URI: `http://product-service.atlas.${environmentName}:8082`,
        ORDER_SERVICE_URI: `http://order-service.atlas.${environmentName}:8083`,
        NOTIFICATION_SERVICE_URI: `http://notification-service.atlas.${environmentName}:8084`,
        // Redis Configuration - using infrastructure outputs
        REDIS_CLUSTER_NODES: `${infrastructure.redisCluster.attrConfigurationEndPointAddress}:6379`,
      },
      secrets: {
        REDIS_PASSWORD: ecs.Secret.fromSecretsManager(infrastructure.redisSecret),
      },
    };
  }

  createAuthServerConfig(environmentName: string, infrastructure: InfrastructureStack): ServiceConfig {
    return {
      serviceName: 'auth-server',
      containerPort: 8091,
      healthCheckPath: '/actuator/health',
      listenerPriority: 101,
      pathPatterns: ['/auth/*'],
      environment: {
        // Database Configuration - using infrastructure outputs
        MYSQL_URL: `jdbc:mysql://${infrastructure.mysqlDatabase.instanceEndpoint.hostname}:3306/db_auth?useUnicode=yes&characterEncoding=UTF-8&allowPublicKeyRetrieval=true&useSSL=false`,
        MYSQL_USERNAME: 'root',
        // Redis Configuration - using infrastructure outputs
        REDIS_CLUSTER_NODES: `${infrastructure.redisCluster.attrConfigurationEndPointAddress}:6379`,
      },
      secrets: {
        MYSQL_PASSWORD: ecs.Secret.fromSecretsManager(infrastructure.mysqlSecret, 'password'),
        REDIS_PASSWORD: ecs.Secret.fromSecretsManager(infrastructure.redisSecret),
      },
    };
  }
}

// Service-specific configuration builders for more complex scenarios
export class ApiGatewayConfigBuilder {
  private config: Partial<ServiceConfig> = {
    serviceName: 'api-gateway',
    containerPort: 8080,
    healthCheckPath: '/actuator/health',
    listenerPriority: 100,
    pathPatterns: ['/*'],
  };

  withServiceUris(environmentName: string): ApiGatewayConfigBuilder {
    this.config.environment = {
      ...this.config.environment,
      AUTH_SERVER_URI: `http://auth-server.atlas.${environmentName}:8091`,
      USER_SERVICE_URI: `http://user-service.atlas.${environmentName}:8081`,
      PRODUCT_SERVICE_URI: `http://product-service.atlas.${environmentName}:8082`,
      ORDER_SERVICE_URI: `http://order-service.atlas.${environmentName}:8083`,
      NOTIFICATION_SERVICE_URI: `http://notification-service.atlas.${environmentName}:8084`,
    };
    return this;
  }

  withRedisConfig(infrastructure: InfrastructureStack): ApiGatewayConfigBuilder {
    this.config.environment = {
      ...this.config.environment,
      REDIS_CLUSTER_NODES: `${infrastructure.redisCluster.attrConfigurationEndPointAddress}:6379`,
    };
    this.config.secrets = {
      ...this.config.secrets,
      REDIS_PASSWORD: ecs.Secret.fromSecretsManager(infrastructure.redisSecret),
    };
    return this;
  }

  withCustomEnvironment(env: Record<string, string>): ApiGatewayConfigBuilder {
    this.config.environment = {
      ...this.config.environment,
      ...env,
    };
    return this;
  }

  withResourceLimits(cpu: number, memory: number, desiredCount: number): ApiGatewayConfigBuilder {
    this.config.taskCpu = cpu;
    this.config.taskMemory = memory;
    this.config.desiredCount = desiredCount;
    return this;
  }

  build(): ServiceConfig {
    if (!this.config.environment) {
      this.config.environment = {};
    }
    return this.config as ServiceConfig;
  }
}

export class AuthServerConfigBuilder {
  private config: Partial<ServiceConfig> = {
    serviceName: 'auth-server',
    containerPort: 8091,
    healthCheckPath: '/actuator/health',
    listenerPriority: 101,
    pathPatterns: ['/auth/*'],
  };

  withDatabaseConfig(infrastructure: InfrastructureStack): AuthServerConfigBuilder {
    this.config.environment = {
      ...this.config.environment,
      MYSQL_URL: `jdbc:mysql://${infrastructure.mysqlDatabase.instanceEndpoint.hostname}:3306/db_auth?useUnicode=yes&characterEncoding=UTF-8&allowPublicKeyRetrieval=true&useSSL=false`,
      MYSQL_USERNAME: 'root',
    };
    this.config.secrets = {
      ...this.config.secrets,
      MYSQL_PASSWORD: ecs.Secret.fromSecretsManager(infrastructure.mysqlSecret, 'password'),
    };
    return this;
  }

  withRedisConfig(infrastructure: InfrastructureStack): AuthServerConfigBuilder {
    this.config.environment = {
      ...this.config.environment,
      REDIS_CLUSTER_NODES: `${infrastructure.redisCluster.attrConfigurationEndPointAddress}:6379`,
    };
    this.config.secrets = {
      ...this.config.secrets,
      REDIS_PASSWORD: ecs.Secret.fromSecretsManager(infrastructure.redisSecret),
    };
    return this;
  }

  withCustomEnvironment(env: Record<string, string>): AuthServerConfigBuilder {
    this.config.environment = {
      ...this.config.environment,
      ...env,
    };
    return this;
  }

  withResourceLimits(cpu: number, memory: number, desiredCount: number): AuthServerConfigBuilder {
    this.config.taskCpu = cpu;
    this.config.taskMemory = memory;
    this.config.desiredCount = desiredCount;
    return this;
  }

  build(): ServiceConfig {
    if (!this.config.environment) {
      this.config.environment = {};
    }
    return this.config as ServiceConfig;
  }
}

// Convenience functions for common configurations
export const createApiGatewayConfig = (environmentName: string, infrastructure: InfrastructureStack): ServiceConfig => {
  return new ApiGatewayConfigBuilder()
    .withServiceUris(environmentName)
    .withRedisConfig(infrastructure)
    .build();
};

export const createAuthServerConfig = (environmentName: string, infrastructure: InfrastructureStack): ServiceConfig => {
  return new AuthServerConfigBuilder()
    .withDatabaseConfig(infrastructure)
    .withRedisConfig(infrastructure)
    .build();
};

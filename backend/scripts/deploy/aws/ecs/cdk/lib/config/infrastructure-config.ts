import * as cdk from 'aws-cdk-lib';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as rds from 'aws-cdk-lib/aws-rds';

export interface InfrastructureConfig {
  environmentName: string;
  region: string;
  account: string;

  // Networking Configuration
  networking: {
    vpcCidr: string;
    maxAzs: number;
    natGateways: number;
  };

  // Data Layer Configuration
  dataLayer: {
    mysql: {
      instanceType: ec2.InstanceType;
      allocatedStorage: number;
      engineVersion: rds.MysqlEngineVersion;
      databaseName: string;
      multiAz: boolean;
      backupRetention: cdk.Duration;
    };
    redis: {
      nodeType: string;
      numCacheNodes: number;
    };
  };

  // Service Discovery Configuration
  serviceDiscovery: {
    services: string[];
  };

  // Monitoring Configuration
  monitoring: {
    logRetention: cdk.Duration;
  };
}

export class InfrastructureConfigBuilder {
  private config: Partial<InfrastructureConfig> = {};

  static forEnvironment(environmentName: string): InfrastructureConfigBuilder {
    const builder = new InfrastructureConfigBuilder();
    builder.config.environmentName = environmentName;
    return builder;
  }

  withRegion(region: string): InfrastructureConfigBuilder {
    this.config.region = region;
    return this;
  }

  withAccount(account: string): InfrastructureConfigBuilder {
    this.config.account = account;
    return this;
  }

  withNetworking(networking: Partial<InfrastructureConfig['networking']>): InfrastructureConfigBuilder {
    this.config.networking = {
      ...this.getDefaultNetworking(),
      ...networking,
    };
    return this;
  }

  withDataLayer(dataLayer: {
    mysql?: Partial<InfrastructureConfig['dataLayer']['mysql']>;
    redis?: Partial<InfrastructureConfig['dataLayer']['redis']>;
  }): InfrastructureConfigBuilder {
    const defaultDataLayer = this.getDefaultDataLayer();
    this.config.dataLayer = {
      mysql: {
        ...defaultDataLayer.mysql,
        ...(dataLayer.mysql || {}),
      },
      redis: {
        ...defaultDataLayer.redis,
        ...(dataLayer.redis || {}),
      },
    };
    return this;
  }

  withServiceDiscovery(serviceDiscovery: Partial<InfrastructureConfig['serviceDiscovery']>): InfrastructureConfigBuilder {
    this.config.serviceDiscovery = {
      ...this.getDefaultServiceDiscovery(),
      ...serviceDiscovery,
    };
    return this;
  }

  withMonitoring(monitoring: Partial<InfrastructureConfig['monitoring']>): InfrastructureConfigBuilder {
    this.config.monitoring = {
      ...this.getDefaultMonitoring(),
      ...monitoring,
    };
    return this;
  }

  build(): InfrastructureConfig {
    if (!this.config.environmentName || !this.config.region || !this.config.account) {
      throw new Error('Environment name, region, and account are required');
    }

    return {
      environmentName: this.config.environmentName,
      region: this.config.region,
      account: this.config.account,
      networking: this.config.networking || this.getDefaultNetworking(),
      dataLayer: this.config.dataLayer || this.getDefaultDataLayer(),
      serviceDiscovery: this.config.serviceDiscovery || this.getDefaultServiceDiscovery(),
      monitoring: this.config.monitoring || this.getDefaultMonitoring(),
    };
  }

  private getDefaultNetworking(): InfrastructureConfig['networking'] {
    return {
      vpcCidr: '10.0.0.0/16',
      maxAzs: 2,
      natGateways: 1,
    };
  }

  private getDefaultMySQLConfig(): InfrastructureConfig['dataLayer']['mysql'] {
    return {
      instanceType: ec2.InstanceType.of(ec2.InstanceClass.T3, ec2.InstanceSize.MICRO),
      allocatedStorage: 20,
      engineVersion: rds.MysqlEngineVersion.VER_8_0_35,
      databaseName: 'db_auth',
      multiAz: false,
      backupRetention: cdk.Duration.days(7),
    };
  }

  private getDefaultRedisConfig(): InfrastructureConfig['dataLayer']['redis'] {
    return {
      nodeType: 'cache.t3.micro',
      numCacheNodes: 1,
    };
  }

  private getDefaultDataLayer(): InfrastructureConfig['dataLayer'] {
    return {
      mysql: this.getDefaultMySQLConfig(),
      redis: this.getDefaultRedisConfig(),
    };
  }

  private getDefaultServiceDiscovery(): InfrastructureConfig['serviceDiscovery'] {
    return {
      services: [
        'api-gateway',
        'user-service',
        'product-service',
        'order-service',
        'notification-service',
      ],
    };
  }

  private getDefaultMonitoring(): InfrastructureConfig['monitoring'] {
    return {
      logRetention: cdk.Duration.days(7),
    };
  }
}

// Environment-specific configurations
export const createDevConfig = (region: string, account: string): InfrastructureConfig => {
  return InfrastructureConfigBuilder
    .forEnvironment('dev')
    .withRegion(region)
    .withAccount(account)
    .build();
};

export const createStgConfig = (region: string, account: string): InfrastructureConfig => {
  return InfrastructureConfigBuilder
    .forEnvironment('stg')
    .withRegion(region)
    .withAccount(account)
    .withNetworking({ natGateways: 2 })
    .withDataLayer({
      mysql: {
        instanceType: ec2.InstanceType.of(ec2.InstanceClass.T3, ec2.InstanceSize.SMALL),
        multiAz: true,
      },
    })
    .build();
};

export const createProdConfig = (region: string, account: string): InfrastructureConfig => {
  return InfrastructureConfigBuilder
    .forEnvironment('prod')
    .withRegion(region)
    .withAccount(account)
    .withNetworking({
      maxAzs: 3,
      natGateways: 3,
    })
    .withDataLayer({
      mysql: {
        instanceType: ec2.InstanceType.of(ec2.InstanceClass.T3, ec2.InstanceSize.MEDIUM),
        allocatedStorage: 100,
        multiAz: true,
        backupRetention: cdk.Duration.days(30),
      },
      redis: {
        nodeType: 'cache.t3.small',
        numCacheNodes: 2,
      },
    })
    .withMonitoring({
      logRetention: cdk.Duration.days(30),
    })
    .build();
};

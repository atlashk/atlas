import * as cdk from 'aws-cdk-lib';
import {Construct} from 'constructs';

// Import constructs
import {Networking} from '../constructs/networking';
import {DataLayer} from '../constructs/data-layer';
import {ComputeLayer} from '../constructs/compute-layer';
import {ServiceDiscovery} from '../constructs/service-discovery';

// Import configuration
import {
  createDevConfig,
  createProdConfig,
  createStgConfig,
  InfrastructureConfig
} from '../config/infrastructure-config';

export interface InfrastructureStackProps extends cdk.StackProps {
  environmentName: string;
}

export class InfrastructureStack extends cdk.Stack {
  // Core infrastructure components
  public readonly networking: Networking;
  public readonly dataLayer: DataLayer;
  public readonly computeLayer: ComputeLayer;
  public readonly serviceDiscovery: ServiceDiscovery;

  // Configuration
  private readonly config: InfrastructureConfig;

  constructor(scope: Construct, id: string, props: InfrastructureStackProps) {
    super(scope, id, props);

    const {environmentName} = props;

    // Load configuration based on environment
    this.config = this.loadConfiguration(environmentName);

    // Create networking layer
    this.networking = new Networking(this, 'Networking', {
      environmentName,
      vpcCidr: this.config.networking.vpcCidr,
      maxAzs: this.config.networking.maxAzs,
      natGateways: this.config.networking.natGateways,
    });

    // Create data layer
    this.dataLayer = new DataLayer(this, 'DataLayer', {
      environmentName,
      vpc: this.networking.vpc,
      databaseSecurityGroup: this.networking.databaseSecurityGroup,
      redisSecurityGroup: this.networking.redisSecurityGroup,
      mysqlConfig: this.config.dataLayer.mysql,
      redisConfig: this.config.dataLayer.redis,
    });

    // Create compute layer
    this.computeLayer = new ComputeLayer(this, 'ComputeLayer', {
      environmentName,
      vpc: this.networking.vpc,
      loadBalancerSecurityGroup: this.networking.loadBalancerSecurityGroup,
      region: this.config.region,
      account: this.config.account,
    });

    // Create service discovery
    this.serviceDiscovery = new ServiceDiscovery(this, 'ServiceDiscovery', {
      environmentName,
      vpc: this.networking.vpc,
      services: this.config.serviceDiscovery.services,
    });

    // Update IAM roles with data layer secrets
    this.updateIAMRolesWithSecrets();

    // Create stack outputs
    this.createOutputs();
  }

  private loadConfiguration(environmentName: string): InfrastructureConfig {
    const region = this.region;
    const account = this.account;

    switch (environmentName.toLowerCase()) {
      case 'dev':
        return createDevConfig(region, account);
      case 'stg':
        return createStgConfig(region, account);
      case 'prod':
        return createProdConfig(region, account);
      default:
        return createDevConfig(region, account);
    }
  }

  private updateIAMRolesWithSecrets(): void {
    // Add specific secret ARNs to IAM roles
    const secretArns = [
      this.dataLayer.mysqlSecret.secretArn,
      this.dataLayer.redisSecret.secretArn,
    ];

    this.computeLayer.taskExecutionRole.addToPolicy(new cdk.aws_iam.PolicyStatement({
      effect: cdk.aws_iam.Effect.ALLOW,
      actions: ['secretsmanager:GetSecretValue'],
      resources: secretArns,
    }));

    this.computeLayer.taskRole.addToPolicy(new cdk.aws_iam.PolicyStatement({
      effect: cdk.aws_iam.Effect.ALLOW,
      actions: ['secretsmanager:GetSecretValue'],
      resources: secretArns,
    }));
  }

  private createOutputs(): void {
    const {environmentName} = this.config;

    // Networking outputs
    new cdk.CfnOutput(this, 'VPCId', {
      value: this.networking.vpc.vpcId,
      exportName: `vpc-${environmentName}`,
    });

    new cdk.CfnOutput(this, 'PublicSubnets', {
      value: this.networking.vpc.publicSubnets.map(subnet => subnet.subnetId).join(','),
      exportName: `public-subnets-${environmentName}`,
    });

    new cdk.CfnOutput(this, 'PrivateSubnets', {
      value: this.networking.vpc.privateSubnets.map(subnet => subnet.subnetId).join(','),
      exportName: `private-subnets-${environmentName}`,
    });

    // Compute outputs
    new cdk.CfnOutput(this, 'ECSClusterName', {
      value: this.computeLayer.cluster.clusterName,
      exportName: `ecs-cluster-${environmentName}`,
    });

    new cdk.CfnOutput(this, 'LoadBalancerDNS', {
      value: this.computeLayer.loadBalancer.loadBalancerDnsName,
      exportName: `load-balancer-dns-${environmentName}`,
    });

    new cdk.CfnOutput(this, 'LoadBalancerListenerArn', {
      value: this.computeLayer.listener.listenerArn,
      exportName: `load-balancer-listener-${environmentName}`,
    });

    new cdk.CfnOutput(this, 'ECSSecurityGroupId', {
      value: this.networking.ecsSecurityGroup.securityGroupId,
      exportName: `ecs-security-group-${environmentName}`,
    });

    new cdk.CfnOutput(this, 'ECSTaskExecutionRoleArn', {
      value: this.computeLayer.taskExecutionRole.roleArn,
      exportName: `ecs-task-execution-role-${environmentName}`,
    });

    new cdk.CfnOutput(this, 'ECSTaskRoleArn', {
      value: this.computeLayer.taskRole.roleArn,
      exportName: `ecs-task-role-${environmentName}`,
    });

    new cdk.CfnOutput(this, 'LogGroupName', {
      value: this.computeLayer.logGroup.logGroupName,
      exportName: `log-group-${environmentName}`,
    });

    // Data layer outputs
    new cdk.CfnOutput(this, 'MySQLPasswordSecretArn', {
      value: this.dataLayer.mysqlSecret.secretArn,
      exportName: `mysql-password-secret-arn-${environmentName}`,
    });

    new cdk.CfnOutput(this, 'RedisPasswordSecretArn', {
      value: this.dataLayer.redisSecret.secretArn,
      exportName: `redis-password-secret-arn-${environmentName}`,
    });

    new cdk.CfnOutput(this, 'MySQLEndpoint', {
      value: this.dataLayer.mysqlDatabase.instanceEndpoint.hostname,
      exportName: `mysql-endpoint-${environmentName}`,
    });

    new cdk.CfnOutput(this, 'RedisEndpoint', {
      value: this.dataLayer.redisCluster.attrConfigurationEndPointAddress,
      exportName: `redis-endpoint-${environmentName}`,
    });

    // Service Discovery outputs
    this.config.serviceDiscovery.services.forEach(serviceName => {
      const service = this.serviceDiscovery.getService(serviceName);
      const outputName = this.toPascalCase(serviceName) + 'DiscoveryServiceId';

      new cdk.CfnOutput(this, outputName, {
        value: service.serviceId,
        exportName: `${serviceName}-discovery-service-id-${environmentName}`,
      });
    });
  }

  private toPascalCase(str: string): string {
    return str
    .split('-')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join('');
  }

  // Convenience getters for backward compatibility
  public get vpc() {
    return this.networking.vpc;
  }

  public get cluster() {
    return this.computeLayer.cluster;
  }

  public get loadBalancer() {
    return this.computeLayer.loadBalancer;
  }

  public get listener() {
    return this.computeLayer.listener;
  }

  public get ecsSecurityGroup() {
    return this.networking.ecsSecurityGroup;
  }

  public get taskExecutionRole() {
    return this.computeLayer.taskExecutionRole;
  }

  public get taskRole() {
    return this.computeLayer.taskRole;
  }

  public get logGroup() {
    return this.computeLayer.logGroup;
  }

  public get mysqlSecret() {
    return this.dataLayer.mysqlSecret;
  }

  public get redisSecret() {
    return this.dataLayer.redisSecret;
  }

  public get mysqlDatabase() {
    return this.dataLayer.mysqlDatabase;
  }

  public get redisCluster() {
    return this.dataLayer.redisCluster;
  }

  public get serviceDiscoveryNamespace() {
    return this.serviceDiscovery.namespace;
  }

  // Service discovery service getters
  public get userServiceDiscoveryService() {
    return this.serviceDiscovery.getService('user-service');
  }

  public get productServiceDiscoveryService() {
    return this.serviceDiscovery.getService('product-service');
  }

  public get orderServiceDiscoveryService() {
    return this.serviceDiscovery.getService('order-service');
  }

  public get notificationServiceDiscoveryService() {
    return this.serviceDiscovery.getService('notification-service');
  }

  public get apiGatewayDiscoveryService() {
    return this.serviceDiscovery.getService('api-gateway');
  }
}

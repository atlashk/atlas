import * as cdk from 'aws-cdk-lib';
import * as ecs from 'aws-cdk-lib/aws-ecs';
import { Construct } from 'constructs';
import { InfrastructureStack } from '../stack/infrastructure-stack';

export interface InternalServiceConfig {
  serviceName: string;
  containerPort: number;
  healthCheckPath: string;
  environment: Record<string, string>;
  secrets?: Record<string, ecs.Secret>;

  // Optional overrides
  taskCpu?: number;
  taskMemory?: number;
  desiredCount?: number;
  imageTag?: string;
  ecrRepository?: string;
}

export interface InternalServiceProps {
  environmentName: string;
  infrastructure: InfrastructureStack;
  serviceConfig: InternalServiceConfig;
}

export class InternalService extends Construct {
  public readonly service: ecs.FargateService;
  public readonly taskDefinition: ecs.FargateTaskDefinition;

  constructor(scope: Construct, id: string, props: InternalServiceProps) {
    super(scope, id);

    const { environmentName, infrastructure, serviceConfig } = props;

    // Get context values with defaults from service config
    const imageTag = this.node.tryGetContext('imageTag') || serviceConfig.imageTag || 'latest';
    const ecrRepository = this.node.tryGetContext('ecrRepository') ||
      serviceConfig.ecrRepository ||
      `your-account-id.dkr.ecr.${cdk.Stack.of(this).region}.amazonaws.com/atlas-${serviceConfig.serviceName}`;
    const desiredCount = parseInt(this.node.tryGetContext('desiredCount') || serviceConfig.desiredCount?.toString() || '1');
    const taskCpu = parseInt(this.node.tryGetContext('taskCpu') || serviceConfig.taskCpu?.toString() || '512');
    const taskMemory = parseInt(this.node.tryGetContext('taskMemory') || serviceConfig.taskMemory?.toString() || '1024');

    // Create task definition
    this.taskDefinition = this.createTaskDefinition(
      environmentName,
      infrastructure,
      serviceConfig,
      taskCpu,
      taskMemory
    );

    // Add container to task definition
    this.addContainer(
      infrastructure,
      serviceConfig,
      ecrRepository,
      imageTag,
      environmentName
    );

    // Create ECS service (internal-only, no ALB attachment)
    this.service = this.createECSService(
      environmentName,
      infrastructure,
      serviceConfig,
      desiredCount
    );

    // Associate with existing CloudMap service for service discovery
    this.associateWithCloudMapService(infrastructure, serviceConfig);
  }

  private createTaskDefinition(
    environmentName: string,
    infrastructure: InfrastructureStack,
    config: InternalServiceConfig,
    taskCpu: number,
    taskMemory: number
  ): ecs.FargateTaskDefinition {
    return new ecs.FargateTaskDefinition(this, 'TaskDefinition', {
      family: `${config.serviceName}-${environmentName}`,
      cpu: taskCpu,
      memoryLimitMiB: taskMemory,
      executionRole: infrastructure.taskExecutionRole,
      taskRole: infrastructure.taskRole,
    });
  }

  private addContainer(
    infrastructure: InfrastructureStack,
    config: InternalServiceConfig,
    ecrRepository: string,
    imageTag: string,
    environmentName: string
  ): void {
    const container = this.taskDefinition.addContainer(config.serviceName, {
      image: ecs.ContainerImage.fromRegistry(`${ecrRepository}:${imageTag}`),
      essential: true,
      logging: ecs.LogDrivers.awsLogs({
        logGroup: infrastructure.logGroup,
        streamPrefix: config.serviceName,
      }),
      environment: {
        SPRING_PROFILES_ACTIVE: environmentName,
        LOG_LEVEL: 'INFO',
        ...config.environment,
      },
      secrets: config.secrets || {},
      healthCheck: {
        command: [
          'CMD-SHELL',
          `curl -f http://localhost:${config.containerPort}${config.healthCheckPath} || exit 1`
        ],
        interval: cdk.Duration.seconds(30),
        timeout: cdk.Duration.seconds(5),
        retries: 5,
        startPeriod: cdk.Duration.seconds(120),
      },
    });

    container.addPortMappings({
      containerPort: config.containerPort,
      protocol: ecs.Protocol.TCP,
    });
  }

  private createECSService(
    environmentName: string,
    infrastructure: InfrastructureStack,
    config: InternalServiceConfig,
    desiredCount: number
  ): ecs.FargateService {
    return new ecs.FargateService(this, 'Service', {
      serviceName: `${config.serviceName}-${environmentName}`,
      cluster: infrastructure.cluster,
      taskDefinition: this.taskDefinition,
      desiredCount,
      vpcSubnets: {
        subnetType: cdk.aws_ec2.SubnetType.PRIVATE_WITH_EGRESS, // Deploy to private subnets
      },
      securityGroups: [infrastructure.ecsSecurityGroup],
      assignPublicIp: false, // No public IP for internal services
      maxHealthyPercent: 200,
      minHealthyPercent: 50,
      enableExecuteCommand: true,
    });
  }

  private associateWithCloudMapService(
    infrastructure: InfrastructureStack,
    config: InternalServiceConfig
  ): void {
    // Get the existing CloudMap service from infrastructure
    const cloudMapService = this.getCloudMapService(infrastructure, config.serviceName);

    // Associate the ECS service with the existing CloudMap service
    this.service.associateCloudMapService({
      service: cloudMapService,
    });
  }

  private getCloudMapService(
    infrastructure: InfrastructureStack,
    serviceName: string
  ): cdk.aws_servicediscovery.Service {
    switch (serviceName) {
      case 'user-service':
        return infrastructure.userServiceDiscoveryService;
      case 'product-service':
        return infrastructure.productServiceDiscoveryService;
      case 'order-service':
        return infrastructure.orderServiceDiscoveryService;
      case 'notification-service':
        return infrastructure.notificationServiceDiscoveryService;
      default:
        throw new Error(`Unknown internal service name: ${serviceName}`);
    }
  }

  public createOutputs(environmentName: string, serviceName: string): void {
    const pascalCaseServiceName = this.toPascalCase(serviceName);

    new cdk.CfnOutput(this, `${pascalCaseServiceName}ServiceName`, {
      value: this.service.serviceName,
      exportName: `${serviceName}-service-name-${environmentName}`,
    });

    new cdk.CfnOutput(this, `${pascalCaseServiceName}ServiceDiscoveryName`, {
      value: `${serviceName}.atlas.${environmentName}`,
      exportName: `${serviceName}-discovery-name-${environmentName}`,
    });
  }

  private toPascalCase(str: string): string {
    return str
      .split('-')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join('');
  }
} 
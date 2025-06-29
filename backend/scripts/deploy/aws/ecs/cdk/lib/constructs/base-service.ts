import * as cdk from 'aws-cdk-lib';
import * as ecs from 'aws-cdk-lib/aws-ecs';
import * as elbv2 from 'aws-cdk-lib/aws-elasticloadbalancingv2';
import { Construct } from 'constructs';
import { InfrastructureStack } from '../stack/infrastructure-stack';

export interface ServiceConfig {
  serviceName: string;
  containerPort: number;
  healthCheckPath: string;
  listenerPriority: number;
  pathPatterns: string[];
  environment: Record<string, string>;
  secrets?: Record<string, ecs.Secret>;

  // Optional overrides
  taskCpu?: number;
  taskMemory?: number;
  desiredCount?: number;
  imageTag?: string;
  ecrRepository?: string;
}

export interface BaseServiceProps {
  environmentName: string;
  infrastructure: InfrastructureStack;
  serviceConfig: ServiceConfig;
}

export class BaseService extends Construct {
  public readonly service: ecs.FargateService;
  public readonly targetGroup: elbv2.ApplicationTargetGroup;
  public readonly taskDefinition: ecs.FargateTaskDefinition;

  constructor(scope: Construct, id: string, props: BaseServiceProps) {
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

    // Create target group
    this.targetGroup = this.createTargetGroup(environmentName, infrastructure, serviceConfig);

    // Create listener rule
    this.createListenerRule(infrastructure, serviceConfig);

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

    // Create ECS service
    this.service = this.createECSService(
      environmentName,
      infrastructure,
      serviceConfig,
      desiredCount
    );

    // Attach target group to service
    this.service.attachToApplicationTargetGroup(this.targetGroup);

    // Associate with existing CloudMap service
    this.associateWithCloudMapService(infrastructure, serviceConfig);
  }

  private createTargetGroup(
    environmentName: string,
    infrastructure: InfrastructureStack,
    config: ServiceConfig
  ): elbv2.ApplicationTargetGroup {
    return new elbv2.ApplicationTargetGroup(this, 'TargetGroup', {
      targetGroupName: `${config.serviceName}-tg-${environmentName}`,
      port: config.containerPort,
      protocol: elbv2.ApplicationProtocol.HTTP,
      vpc: infrastructure.vpc,
      targetType: elbv2.TargetType.IP,
      healthCheck: {
        path: config.healthCheckPath,
        protocol: elbv2.Protocol.HTTP,
        interval: cdk.Duration.seconds(30),
        timeout: cdk.Duration.seconds(5),
        healthyThresholdCount: 2,
        unhealthyThresholdCount: 3,
      },
    });
  }

  private createListenerRule(
    infrastructure: InfrastructureStack,
    config: ServiceConfig
  ): void {
    new elbv2.ApplicationListenerRule(this, 'ListenerRule', {
      listener: infrastructure.listener,
      priority: config.listenerPriority,
      conditions: [
        elbv2.ListenerCondition.pathPatterns(config.pathPatterns),
      ],
      action: elbv2.ListenerAction.forward([this.targetGroup]),
    });
  }

  private createTaskDefinition(
    environmentName: string,
    infrastructure: InfrastructureStack,
    config: ServiceConfig,
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
    config: ServiceConfig,
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
    });

    container.addPortMappings({
      containerPort: config.containerPort,
      protocol: ecs.Protocol.TCP,
    });
  }

  private createECSService(
    environmentName: string,
    infrastructure: InfrastructureStack,
    config: ServiceConfig,
    desiredCount: number
  ): ecs.FargateService {
    return new ecs.FargateService(this, 'Service', {
      serviceName: `${config.serviceName}-${environmentName}`,
      cluster: infrastructure.cluster,
      taskDefinition: this.taskDefinition,
      desiredCount,
      vpcSubnets: {
        subnetType: cdk.aws_ec2.SubnetType.PUBLIC,
      },
      securityGroups: [infrastructure.ecsSecurityGroup],
      assignPublicIp: true,
      maxHealthyPercent: 200,
      minHealthyPercent: 50,
      enableExecuteCommand: true,
    });
  }

  public createOutputs(environmentName: string, infrastructure: InfrastructureStack, serviceName: string): void {
    const pascalCaseServiceName = this.toPascalCase(serviceName);

    new cdk.CfnOutput(this, `${pascalCaseServiceName}ServiceName`, {
      value: this.service.serviceName,
      exportName: `${serviceName}-service-name-${environmentName}`,
    });

    new cdk.CfnOutput(this, `${pascalCaseServiceName}TargetGroupArn`, {
      value: this.targetGroup.targetGroupArn,
      exportName: `${serviceName}-target-group-${environmentName}`,
    });

    new cdk.CfnOutput(this, `${pascalCaseServiceName}URL`, {
      value: `http://${infrastructure.loadBalancer.loadBalancerDnsName}`,
      exportName: `${serviceName}-url-${environmentName}`,
    });
  }

  private associateWithCloudMapService(
    infrastructure: InfrastructureStack,
    config: ServiceConfig
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
      case 'auth-server':
        return infrastructure.authServerDiscoveryService;
      case 'api-gateway':
        return infrastructure.apiGatewayDiscoveryService;
      case 'user-service':
        return infrastructure.userServiceDiscoveryService;
      case 'product-service':
        return infrastructure.productServiceDiscoveryService;
      case 'order-service':
        return infrastructure.orderServiceDiscoveryService;
      case 'notification-service':
        return infrastructure.notificationServiceDiscoveryService;
      default:
        throw new Error(`Unknown service name: ${serviceName}`);
    }
  }

  private toPascalCase(str: string): string {
    return str
      .split('-')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join('');
  }
} 
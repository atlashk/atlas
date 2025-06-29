import * as cdk from 'aws-cdk-lib';
import * as ecs from 'aws-cdk-lib/aws-ecs';
import * as elbv2 from 'aws-cdk-lib/aws-elasticloadbalancingv2';
import { Construct } from 'constructs';
import { InfrastructureStack } from './infrastructure-stack';
import { BaseService } from '../constructs/base-service';
import { createApiGatewayConfig } from '../config/service-configs';

export interface ApiGatewayStackProps extends cdk.StackProps {
  environmentName: string;
  infrastructure: InfrastructureStack;
}

export class ApiGatewayStack extends cdk.Stack {
  public readonly service: ecs.FargateService;
  public readonly targetGroup: elbv2.ApplicationTargetGroup;
  private readonly baseService: BaseService;

  constructor(scope: Construct, id: string, props: ApiGatewayStackProps) {
    super(scope, id, props);

    const { environmentName, infrastructure } = props;

    // Create service configuration using the existing config system
    const serviceConfig = createApiGatewayConfig(environmentName, infrastructure);

    // Create the base service
    this.baseService = new BaseService(this, 'api-gateway', {
      environmentName,
      infrastructure,
      serviceConfig,
    });

    // Expose service and target group for backward compatibility
    this.service = this.baseService.service;
    this.targetGroup = this.baseService.targetGroup;

    // Create outputs using the base service
    this.baseService.createOutputs(environmentName, infrastructure, 'api-gateway');

    // Add API Gateway specific outputs
    this.createAdditionalOutputs(environmentName, infrastructure);
  }

  private createAdditionalOutputs(environmentName: string, infrastructure: InfrastructureStack): void {
    new cdk.CfnOutput(this, 'ApiGatewayHealthCheckURL', {
      value: `http://${infrastructure.loadBalancer.loadBalancerDnsName}/actuator/health`,
      exportName: `api-gateway-health-check-url-${environmentName}`,
    });

    new cdk.CfnOutput(this, 'ApiGatewaySwaggerURL', {
      value: `http://${infrastructure.loadBalancer.loadBalancerDnsName}/swagger-ui.html`,
      exportName: `api-gateway-swagger-url-${environmentName}`,
    });
  }
}

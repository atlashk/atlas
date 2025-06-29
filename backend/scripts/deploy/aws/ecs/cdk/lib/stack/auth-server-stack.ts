import * as cdk from 'aws-cdk-lib';
import * as ecs from 'aws-cdk-lib/aws-ecs';
import * as elbv2 from 'aws-cdk-lib/aws-elasticloadbalancingv2';
import { Construct } from 'constructs';
import { InfrastructureStack } from './infrastructure-stack';
import { BaseService } from '../constructs/base-service';
import { createAuthServerConfig } from '../config/service-configs';

export interface AuthServerStackProps extends cdk.StackProps {
  environmentName: string;
  infrastructure: InfrastructureStack;
}

export class AuthServerStack extends cdk.Stack {
  public readonly service: ecs.FargateService;
  public readonly targetGroup: elbv2.ApplicationTargetGroup;
  private readonly baseService: BaseService;

  constructor(scope: Construct, id: string, props: AuthServerStackProps) {
    super(scope, id, props);

    const { environmentName, infrastructure } = props;

    // Create service configuration using the existing config system
    const serviceConfig = createAuthServerConfig(environmentName, infrastructure);

    // Create the base service
    this.baseService = new BaseService(this, 'auth-server', {
      environmentName,
      infrastructure,
      serviceConfig,
    });

    // Expose service and target group for backward compatibility
    this.service = this.baseService.service;
    this.targetGroup = this.baseService.targetGroup;

    // Create outputs using the base service
    this.baseService.createOutputs(environmentName, infrastructure, 'auth-server');

    // Add auth-server specific outputs
    this.createAdditionalOutputs(environmentName, infrastructure);
  }

  private createAdditionalOutputs(environmentName: string, infrastructure: InfrastructureStack): void {
    new cdk.CfnOutput(this, 'AuthServerHealthCheckURL', {
      value: `http://${infrastructure.loadBalancer.loadBalancerDnsName}/auth/actuator/health`,
      exportName: `auth-server-health-check-url-${environmentName}`,
    });
  }
}

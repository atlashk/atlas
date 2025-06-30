import * as cdk from 'aws-cdk-lib';
import * as ecs from 'aws-cdk-lib/aws-ecs';
import * as elbv2 from 'aws-cdk-lib/aws-elasticloadbalancingv2';
import { Construct } from 'constructs';
import { InfrastructureStack } from './infrastructure-stack';
import { InternalService } from '../constructs/internal-service';
import { createAuthServerConfig } from '../config/service-configs';

export interface AuthServerStackProps extends cdk.StackProps {
  environmentName: string;
  infrastructure: InfrastructureStack;
}

export class AuthServerStack extends cdk.Stack {
  public readonly service: ecs.FargateService;
  private readonly authServerService: InternalService;

  constructor(scope: Construct, id: string, props: AuthServerStackProps) {
    super(scope, id, props);

    const { environmentName, infrastructure } = props;

    // Create service configuration using the existing config system
    const serviceConfig = createAuthServerConfig(environmentName, infrastructure);

    // Create the auth server service (internal service accessed through API Gateway)
    this.authServerService = new InternalService(this, 'auth-server', {
      environmentName,
      infrastructure,
      serviceConfig,
    });

    // Expose service for backward compatibility
    this.service = this.authServerService.service;

    // Create outputs using the auth server service
    this.authServerService.createOutputs(environmentName, 'auth-server');

    // Add auth-server specific outputs
    this.createAdditionalOutputs(environmentName, infrastructure);
  }

  private createAdditionalOutputs(environmentName: string, infrastructure: InfrastructureStack): void {
    new cdk.CfnOutput(this, 'AuthServerServiceDiscoveryName', {
      value: `auth-server.atlas.${environmentName}`,
      exportName: `auth-server-discovery-name-${environmentName}`,
    });

    new cdk.CfnOutput(this, 'AuthServerAccessURL', {
      value: `http://${infrastructure.loadBalancer.loadBalancerDnsName}/api/auth/`,
      exportName: `auth-server-access-url-${environmentName}`,
      description: 'Auth server is accessible through API Gateway at this URL',
    });
  }
}

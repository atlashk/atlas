import * as cdk from 'aws-cdk-lib';
import * as ecs from 'aws-cdk-lib/aws-ecs';
import { Construct } from 'constructs';
import { InfrastructureStack } from './infrastructure-stack';
import { InternalService } from '../constructs/internal-service';
import { createUserServiceConfig } from '../config/service-configs';

export interface UserServiceStackProps extends cdk.StackProps {
  environmentName: string;
  infrastructure: InfrastructureStack;
}

export class UserServiceStack extends cdk.Stack {
  public readonly service: ecs.FargateService;
  private readonly internalService: InternalService;

  constructor(scope: Construct, id: string, props: UserServiceStackProps) {
    super(scope, id, props);

    const { environmentName, infrastructure } = props;

    // Create service configuration
    const serviceConfig = createUserServiceConfig(environmentName, infrastructure);

    // Create the internal service (no ALB listener rules)
    this.internalService = new InternalService(this, 'user-service', {
      environmentName,
      infrastructure,
      serviceConfig,
    });

    // Expose service for backward compatibility
    this.service = this.internalService.service;

    // Create outputs
    this.internalService.createOutputs(environmentName, 'user-service');
  }
} 
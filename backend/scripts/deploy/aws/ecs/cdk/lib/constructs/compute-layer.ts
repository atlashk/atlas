import * as cdk from 'aws-cdk-lib';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as ecs from 'aws-cdk-lib/aws-ecs';
import * as elbv2 from 'aws-cdk-lib/aws-elasticloadbalancingv2';
import * as iam from 'aws-cdk-lib/aws-iam';
import * as logs from 'aws-cdk-lib/aws-logs';
import { Construct } from 'constructs';

export interface ComputeLayerProps {
  environmentName: string;
  vpc: ec2.Vpc;
  loadBalancerSecurityGroup: ec2.SecurityGroup;
  region: string;
  account: string;
}

export class ComputeLayer extends Construct {
  public readonly cluster: ecs.Cluster;
  public readonly loadBalancer: elbv2.ApplicationLoadBalancer;
  public readonly listener: elbv2.ApplicationListener;
  public readonly taskExecutionRole: iam.Role;
  public readonly taskRole: iam.Role;
  public readonly logGroup: logs.LogGroup;

  constructor(scope: Construct, id: string, props: ComputeLayerProps) {
    super(scope, id);

    const { environmentName, vpc, loadBalancerSecurityGroup, region, account } = props;

    // Create ECS cluster
    this.cluster = new ecs.Cluster(this, 'ECSCluster', {
      vpc,
      clusterName: `${environmentName}-atlas-cluster`,
    });

    // Create Application Load Balancer
    this.loadBalancer = new elbv2.ApplicationLoadBalancer(this, 'ApplicationLoadBalancer', {
      vpc,
      internetFacing: true,
      loadBalancerName: `${environmentName}-atlas-alb`,
      securityGroup: loadBalancerSecurityGroup,
    });

    // Create Load Balancer Listener
    this.listener = this.loadBalancer.addListener('LoadBalancerListener', {
      port: 80,
      protocol: elbv2.ApplicationProtocol.HTTP,
      defaultAction: elbv2.ListenerAction.fixedResponse(404, {
        contentType: 'text/plain',
        messageBody: 'Service not found',
      }),
    });

    // Create CloudWatch Log Group
    this.logGroup = new logs.LogGroup(this, 'LogGroup', {
      logGroupName: `/ecs/atlas/${environmentName}`,
      retention: logs.RetentionDays.ONE_WEEK,
      removalPolicy: cdk.RemovalPolicy.DESTROY,
    });

    // Create IAM roles
    const roles = this.createIAMRoles(environmentName, region, account);
    this.taskExecutionRole = roles.taskExecutionRole;
    this.taskRole = roles.taskRole;
  }

  private createIAMRoles(environmentName: string, region: string, account: string) {
    // ECS Task Execution Role
    const taskExecutionRole = new iam.Role(this, 'ECSTaskExecutionRole', {
      assumedBy: new iam.ServicePrincipal('ecs-tasks.amazonaws.com'),
      managedPolicies: [
        iam.ManagedPolicy.fromAwsManagedPolicyName('service-role/AmazonECSTaskExecutionRolePolicy'),
      ],
    });

    taskExecutionRole.addToPolicy(new iam.PolicyStatement({
      effect: iam.Effect.ALLOW,
      actions: ['secretsmanager:GetSecretValue'],
      resources: [
        `arn:aws:secretsmanager:${region}:${account}:secret:atlas/${environmentName}/*`,
      ],
    }));

    // ECS Task Role
    const taskRole = new iam.Role(this, 'ECSTaskRole', {
      assumedBy: new iam.ServicePrincipal('ecs-tasks.amazonaws.com'),
    });

    taskRole.addToPolicy(new iam.PolicyStatement({
      effect: iam.Effect.ALLOW,
      actions: ['cloudwatch:PutMetricData'],
      resources: ['*'],
    }));

    taskRole.addToPolicy(new iam.PolicyStatement({
      effect: iam.Effect.ALLOW,
      actions: ['secretsmanager:GetSecretValue'],
      resources: [
        `arn:aws:secretsmanager:${region}:${account}:secret:atlas/${environmentName}/*`,
      ],
    }));

    return { taskExecutionRole, taskRole };
  }
} 
import * as cdk from 'aws-cdk-lib';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import { Construct } from 'constructs';

export interface NetworkingProps {
  environmentName: string;
  vpcCidr?: string;
  maxAzs?: number;
  natGateways?: number;
}

export class Networking extends Construct {
  public readonly vpc: ec2.Vpc;
  public readonly loadBalancerSecurityGroup: ec2.SecurityGroup;
  public readonly ecsSecurityGroup: ec2.SecurityGroup;
  public readonly databaseSecurityGroup: ec2.SecurityGroup;
  public readonly redisSecurityGroup: ec2.SecurityGroup;

  constructor(scope: Construct, id: string, props: NetworkingProps) {
    super(scope, id);

    const { environmentName, vpcCidr = '10.0.0.0/16', maxAzs = 2, natGateways = 1 } = props;

    // VPC with public and private subnets
    this.vpc = new ec2.Vpc(this, 'VPC', {
      cidr: vpcCidr,
      maxAzs,
      natGateways,
      subnetConfiguration: [
        {
          cidrMask: 24,
          name: 'Public',
          subnetType: ec2.SubnetType.PUBLIC,
        },
        {
          cidrMask: 24,
          name: 'Private',
          subnetType: ec2.SubnetType.PRIVATE_WITH_EGRESS,
        },
      ],
    });

    // Create security groups
    const securityGroups = this.createSecurityGroups();
    this.loadBalancerSecurityGroup = securityGroups.loadBalancerSecurityGroup;
    this.ecsSecurityGroup = securityGroups.ecsSecurityGroup;
    this.databaseSecurityGroup = securityGroups.databaseSecurityGroup;
    this.redisSecurityGroup = securityGroups.redisSecurityGroup;
  }

  private createSecurityGroups() {
    // Load Balancer Security Group
    const loadBalancerSecurityGroup = new ec2.SecurityGroup(this, 'LoadBalancerSecurityGroup', {
      vpc: this.vpc,
      description: 'Security group for Application Load Balancer',
      allowAllOutbound: true,
    });
    loadBalancerSecurityGroup.addIngressRule(
      ec2.Peer.anyIpv4(),
      ec2.Port.tcp(80),
      'Allow HTTP traffic'
    );

    // ECS Security Group
    const ecsSecurityGroup = new ec2.SecurityGroup(this, 'ECSSecurityGroup', {
      vpc: this.vpc,
      description: 'Security group for ECS tasks',
      allowAllOutbound: true,
    });
    ecsSecurityGroup.addIngressRule(
      loadBalancerSecurityGroup,
      ec2.Port.tcpRange(8080),
      'Allow traffic from load balancer'
    );
    // Allow internal communication between ECS services
    ecsSecurityGroup.addIngressRule(
      ecsSecurityGroup,
      ec2.Port.tcpRange(8080),
      'Allow internal communication between ECS services'
    );

    // Database Security Group
    const databaseSecurityGroup = new ec2.SecurityGroup(this, 'DatabaseSecurityGroup', {
      vpc: this.vpc,
      description: 'Security group for RDS MySQL database',
      allowAllOutbound: false,
    });
    databaseSecurityGroup.addIngressRule(
      ecsSecurityGroup,
      ec2.Port.tcp(3306),
      'Allow MySQL traffic from ECS'
    );

    // Redis Security Group
    const redisSecurityGroup = new ec2.SecurityGroup(this, 'RedisSecurityGroup', {
      vpc: this.vpc,
      description: 'Security group for Redis cluster',
      allowAllOutbound: false,
    });
    redisSecurityGroup.addIngressRule(
      ecsSecurityGroup,
      ec2.Port.tcp(6379),
      'Allow Redis traffic from ECS'
    );

    return {
      loadBalancerSecurityGroup,
      ecsSecurityGroup,
      databaseSecurityGroup,
      redisSecurityGroup,
    };
  }
}

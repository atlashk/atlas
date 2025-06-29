import * as cdk from 'aws-cdk-lib';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as rds from 'aws-cdk-lib/aws-rds';
import * as elasticache from 'aws-cdk-lib/aws-elasticache';
import * as secretsmanager from 'aws-cdk-lib/aws-secretsmanager';
import { Construct } from 'constructs';

export interface DataLayerProps {
  environmentName: string;
  vpc: ec2.Vpc;
  databaseSecurityGroup: ec2.SecurityGroup;
  redisSecurityGroup: ec2.SecurityGroup;
  mysqlConfig?: {
    instanceType?: ec2.InstanceType;
    allocatedStorage?: number;
    engineVersion?: rds.MysqlEngineVersion;
    databaseName?: string;
    multiAz?: boolean;
    backupRetention?: cdk.Duration;
  };
  redisConfig?: {
    nodeType?: string;
    numCacheNodes?: number;
  };
}

export class DataLayer extends Construct {
  public readonly mysqlSecret: secretsmanager.Secret;
  public readonly redisSecret: secretsmanager.Secret;
  public readonly mysqlDatabase: rds.DatabaseInstance;
  public readonly redisCluster: elasticache.CfnReplicationGroup;

  constructor(scope: Construct, id: string, props: DataLayerProps) {
    super(scope, id);

    const {
      environmentName,
      vpc,
      databaseSecurityGroup,
      redisSecurityGroup,
      mysqlConfig = {},
      redisConfig = {}
    } = props;

    // Create secrets
    const secrets = this.createSecrets(environmentName);
    this.mysqlSecret = secrets.mysqlSecret;
    this.redisSecret = secrets.redisSecret;

    // Create MySQL database
    this.mysqlDatabase = this.createMySQLDatabase(environmentName, vpc, databaseSecurityGroup, mysqlConfig);

    // Create Redis cluster
    this.redisCluster = this.createRedisCluster(environmentName, vpc, redisSecurityGroup, redisConfig);
  }

  private createSecrets(environmentName: string) {
    // MySQL Secret
    const mysqlSecret = new secretsmanager.Secret(this, 'MySQLPasswordSecret', {
      secretName: `atlas/${environmentName}/mysql-password`,
      description: `MySQL password for Atlas ${environmentName} environment`,
      generateSecretString: {
        secretStringTemplate: JSON.stringify({ username: 'root' }),
        generateStringKey: 'password',
        passwordLength: 32,
        excludeCharacters: '"@/\\\'',
      },
    });

    // Redis Secret
    const redisSecret = new secretsmanager.Secret(this, 'RedisPasswordSecret', {
      secretName: `atlas/${environmentName}/redis-password`,
      description: `Redis password for Atlas ${environmentName} environment`,
      generateSecretString: {
        passwordLength: 32,
        excludeCharacters: '"@/\\\'',
      },
    });

    return { mysqlSecret, redisSecret };
  }

  private createMySQLDatabase(
    environmentName: string,
    vpc: ec2.Vpc,
    securityGroup: ec2.SecurityGroup,
    config: any
  ) {
    const {
      instanceType = ec2.InstanceType.of(ec2.InstanceClass.T3, ec2.InstanceSize.MICRO),
      allocatedStorage = 20,
      engineVersion = rds.MysqlEngineVersion.VER_8_0_35,
      databaseName = 'db_auth',
      multiAz = false,
      backupRetention = cdk.Duration.days(7)
    } = config;

    return new rds.DatabaseInstance(this, 'MySQLDatabase', {
      instanceIdentifier: `atlas-${environmentName}-mysql`,
      engine: rds.DatabaseInstanceEngine.mysql({ version: engineVersion }),
      instanceType,
      credentials: rds.Credentials.fromSecret(this.mysqlSecret),
      allocatedStorage,
      storageType: rds.StorageType.GP2,
      storageEncrypted: true,
      databaseName,
      vpc,
      vpcSubnets: {
        subnetType: ec2.SubnetType.PUBLIC,
      },
      securityGroups: [securityGroup],
      backupRetention,
      multiAz,
      publiclyAccessible: true,
      deletionProtection: false,
      removalPolicy: cdk.RemovalPolicy.DESTROY,
    });
  }

  private createRedisCluster(
    environmentName: string,
    vpc: ec2.Vpc,
    securityGroup: ec2.SecurityGroup,
    config: any
  ) {
    const {
      nodeType = 'cache.t3.micro',
      numCacheNodes = 3 // Minimum 3 nodes for Redis cluster mode
    } = config;

    // Ensure minimum nodes for cluster mode
    const clusterNodes = Math.max(numCacheNodes, 3);

    // Redis Subnet Group
    const redisSubnetGroup = new elasticache.CfnSubnetGroup(this, 'RedisSubnetGroup', {
      description: 'Subnet group for Atlas Redis cluster',
      subnetIds: vpc.privateSubnets.map(subnet => subnet.subnetId),
    });

    // Redis Cluster (using ReplicationGroup in cluster mode)
    return new elasticache.CfnReplicationGroup(this, 'RedisCluster', {
      replicationGroupId: `atlas-${environmentName}-redis`,
      replicationGroupDescription: `Atlas Redis cluster for ${environmentName} environment`,
      cacheNodeType: nodeType,
      engine: 'redis',
      engineVersion: '7.0',
      cacheSubnetGroupName: redisSubnetGroup.ref,
      securityGroupIds: [securityGroup.securityGroupId],
      port: 6379,
      // Enable cluster mode
      clusterMode: 'enabled',
      numNodeGroups: Math.ceil(clusterNodes / 2), // Number of shards
      replicasPerNodeGroup: 1, // Number of replicas per shard
      // Enable automatic failover and multi-AZ
      automaticFailoverEnabled: true,
      multiAzEnabled: true,
      // Enable auth token (password)
      authToken: this.redisSecret.secretValue.unsafeUnwrap(),
      transitEncryptionEnabled: true,
      atRestEncryptionEnabled: true,
    });
  }
}

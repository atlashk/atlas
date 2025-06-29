#!/usr/bin/env node
import * as cdk from 'aws-cdk-lib';
import { InfrastructureStack } from '../lib/stack/infrastructure-stack';
import { ApiGatewayStack } from '../lib/stack/api-gateway-stack';
import { AuthServerStack } from '../lib/stack/auth-server-stack';

const app = new cdk.App();

// Get context values
const environmentName = app.node.tryGetContext('environment') || 'dev';
const region = app.node.tryGetContext('region') || 'us-east-1';
const account = app.node.tryGetContext('account') || process.env.CDK_DEFAULT_ACCOUNT;

const env = { account, region };

// Infrastructure Stack (VPC, RDS, Redis, ECS Cluster, etc.)
const infrastructureStack = new InfrastructureStack(app, `atlas-infrastructure-${environmentName}`, {
  env,
  environmentName,
  description: `Atlas Microservices - Base Infrastructure for ECS Deployment (${environmentName})`,
});

// API Gateway Stack
const apiGatewayStack = new ApiGatewayStack(app, `atlas-api-gateway-${environmentName}`, {
  env,
  environmentName,
  infrastructure: infrastructureStack,
  description: `Atlas API Gateway - ECS Deployment (${environmentName})`,
});

// Auth Server Stack
const authServerStack = new AuthServerStack(app, `atlas-auth-server-${environmentName}`, {
  env,
  environmentName,
  infrastructure: infrastructureStack,
  description: `Atlas Auth Server - ECS Deployment (${environmentName})`,
});

// Add dependencies
apiGatewayStack.addDependency(infrastructureStack);
authServerStack.addDependency(infrastructureStack);

// Add tags to all stacks
cdk.Tags.of(app).add('Environment', environmentName);
cdk.Tags.of(app).add('Application', 'Atlas');
cdk.Tags.of(app).add('ManagedBy', 'CDK');

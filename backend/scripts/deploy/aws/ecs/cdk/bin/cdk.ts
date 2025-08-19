#!/usr/bin/env node
import * as cdk from 'aws-cdk-lib';
import {InfrastructureStack} from '../lib/stack/infrastructure-stack';
import {ApiGatewayStack} from '../lib/stack/api-gateway-stack';
import {UserServiceStack} from '../lib/stack/user-service-stack';
import {ProductServiceStack} from '../lib/stack/product-service-stack';
import {OrderServiceStack} from '../lib/stack/order-service-stack';
import {NotificationServiceStack} from '../lib/stack/notification-service-stack';

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

// Downstream Service Stacks (Internal Services - No ALB Listener Rules)
const userServiceStack = new UserServiceStack(app, `atlas-user-service-${environmentName}`, {
  env,
  environmentName,
  infrastructure: infrastructureStack,
  description: `Atlas User Service - ECS Deployment (${environmentName})`,
});

const productServiceStack = new ProductServiceStack(app, `atlas-product-service-${environmentName}`, {
  env,
  environmentName,
  infrastructure: infrastructureStack,
  description: `Atlas Product Service - ECS Deployment (${environmentName})`,
});

const orderServiceStack = new OrderServiceStack(app, `atlas-order-service-${environmentName}`, {
  env,
  environmentName,
  infrastructure: infrastructureStack,
  description: `Atlas Order Service - ECS Deployment (${environmentName})`,
});

const notificationServiceStack = new NotificationServiceStack(app, `atlas-notification-service-${environmentName}`, {
  env,
  environmentName,
  infrastructure: infrastructureStack,
  description: `Atlas Notification Service - ECS Deployment (${environmentName})`,
});

// Add dependencies
apiGatewayStack.addDependency(infrastructureStack);
authServerStack.addDependency(infrastructureStack);
userServiceStack.addDependency(infrastructureStack);
productServiceStack.addDependency(infrastructureStack);
orderServiceStack.addDependency(infrastructureStack);
notificationServiceStack.addDependency(infrastructureStack);

// API Gateway should be deployed after downstream services for proper service discovery
apiGatewayStack.addDependency(userServiceStack);
apiGatewayStack.addDependency(productServiceStack);
apiGatewayStack.addDependency(orderServiceStack);
apiGatewayStack.addDependency(notificationServiceStack);

// Add tags to all stacks
cdk.Tags.of(app).add('Environment', environmentName);
cdk.Tags.of(app).add('Application', 'Atlas');
cdk.Tags.of(app).add('ManagedBy', 'CDK');

# Product Service Module Outputs

# ECS Cluster Outputs
output "cluster_id" {
  description = "ID of the ECS cluster"
  value       = aws_ecs_cluster.product_service.id
}

output "cluster_name" {
  description = "Name of the ECS cluster"
  value       = aws_ecs_cluster.product_service.name
}

# ALB Outputs
output "alb_arn" {
  description = "ARN of the Application Load Balancer"
  value       = aws_lb.product_service.arn
}

output "alb_dns_name" {
  description = "DNS name of the Application Load Balancer"
  value       = aws_lb.product_service.dns_name
}

output "alb_zone_id" {
  description = "Zone ID of the Application Load Balancer"
  value       = aws_lb.product_service.zone_id
}

output "alb_listener_arn" {
  description = "ARN of the ALB listener"
  value       = aws_lb_listener.product_service.arn
}

# Security Group Outputs
output "alb_security_group_id" {
  description = "ID of the ALB security group"
  value       = aws_security_group.alb.id
}

output "ecs_security_group_id" {
  description = "ID of the ECS security group"
  value       = aws_security_group.ecs.id
}

# Target Group Outputs
output "target_group_arn" {
  description = "ARN of the target group"
  value       = aws_lb_target_group.product_service.arn
}

output "target_group_name" {
  description = "Name of the target group"
  value       = aws_lb_target_group.product_service.name
}

# ECS Service Outputs
output "service_name" {
  description = "Name of the ECS service"
  value       = aws_ecs_service.product_service.name
}

output "service_arn" {
  description = "ARN of the ECS service"
  value       = aws_ecs_service.product_service.id
}

# Task Definition Outputs
output "task_definition_arn" {
  description = "ARN of the ECS task definition"
  value       = aws_ecs_task_definition.product_service.arn
}

# IAM Role Outputs
output "ecs_task_execution_role_arn" {
  description = "ARN of the ECS task execution role"
  value       = module.product_service_iam.ecs_task_execution_role_arn
}

output "ecs_task_role_arn" {
  description = "ARN of the ECS task role"
  value       = module.product_service_iam.ecs_task_role_arn
}

# CloudWatch Log Group Output
output "log_group_name" {
  description = "Name of the CloudWatch log group"
  value       = aws_cloudwatch_log_group.product_service.name
}
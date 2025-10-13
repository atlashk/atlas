# User Service Module Outputs

# ECS Cluster
output "ecs_cluster_id" {
  description = "ECS Cluster ID"
  value       = aws_ecs_cluster.user_service.id
}

output "ecs_cluster_name" {
  description = "ECS Cluster Name"
  value       = aws_ecs_cluster.user_service.name
}

# Application Load Balancer
output "alb_arn" {
  description = "ALB ARN"
  value       = aws_lb.user_service.arn
}

output "alb_dns_name" {
  description = "ALB DNS Name"
  value       = aws_lb.user_service.dns_name
}

output "alb_zone_id" {
  description = "ALB Zone ID"
  value       = aws_lb.user_service.zone_id
}

output "alb_listener_arn" {
  description = "ALB Listener ARN"
  value       = aws_lb_listener.user_service.arn
}

# Security Groups
output "alb_security_group_id" {
  description = "ALB Security Group ID"
  value       = aws_security_group.alb.id
}

output "ecs_security_group_id" {
  description = "ECS Security Group ID"
  value       = aws_security_group.ecs.id
}

# Target Group
output "target_group_arn" {
  description = "Target Group ARN"
  value       = aws_lb_target_group.user_service.arn
}

output "target_group_name" {
  description = "Target Group Name"
  value       = aws_lb_target_group.user_service.name
}

# ECS Service
output "service_name" {
  description = "ECS Service Name"
  value       = aws_ecs_service.user_service.name
}

output "service_arn" {
  description = "ECS Service ARN"
  value       = aws_ecs_service.user_service.id
}

# Task Definition
output "task_definition_arn" {
  description = "Task Definition ARN"
  value       = aws_ecs_task_definition.user_service.arn
}

# IAM Roles
output "ecs_task_execution_role_arn" {
  description = "ECS Task Execution Role ARN"
  value       = aws_iam_role.ecs_task_execution_role.arn
}

output "ecs_task_role_arn" {
  description = "ECS Task Role ARN"
  value       = aws_iam_role.ecs_task_role.arn
}

# CloudWatch Log Group
output "cloudwatch_log_group_name" {
  description = "CloudWatch Log Group Name"
  value       = aws_cloudwatch_log_group.user_service.name
}
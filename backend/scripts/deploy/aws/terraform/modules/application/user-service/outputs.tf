# User Service Module Outputs

# ECS Cluster Outputs - Only keeping what's used in autoscaling
output "ecs_cluster_name" {
  description = "Name of the ECS cluster"
  value       = aws_ecs_cluster.user_service.name
}

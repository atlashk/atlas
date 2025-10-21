# Product Service Module Outputs

# ECS Cluster Outputs - Only keeping what's used in autoscaling
output "cluster_name" {
  description = "Name of the ECS cluster"
  value       = aws_ecs_cluster.product_service.name
}

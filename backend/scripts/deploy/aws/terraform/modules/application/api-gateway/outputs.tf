# API Gateway Module Outputs

# ECS Cluster
output "ecs_cluster_id" {
  description = "ECS Cluster ID"
  value       = aws_ecs_cluster.api_gateway.id
}

output "ecs_cluster_name" {
  description = "ECS Cluster Name"
  value       = aws_ecs_cluster.api_gateway.name
}

# Application Load Balancer
output "alb_arn" {
  description = "ALB ARN"
  value       = aws_lb.api_gateway.arn
}

output "alb_dns_name" {
  description = "ALB DNS Name"
  value       = aws_lb.api_gateway.dns_name
}

output "alb_zone_id" {
  description = "ALB Zone ID"
  value       = aws_lb.api_gateway.zone_id
}

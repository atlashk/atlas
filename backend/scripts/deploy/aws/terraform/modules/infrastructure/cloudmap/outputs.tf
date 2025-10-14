# Cloud Map Module Outputs

# Namespace outputs
output "namespace_id" {
  description = "The ID of the Cloud Map namespace"
  value       = aws_service_discovery_private_dns_namespace.atlas.id
}

output "namespace_name" {
  description = "The name of the Cloud Map namespace"
  value       = aws_service_discovery_private_dns_namespace.atlas.name
}

output "namespace_arn" {
  description = "The ARN of the Cloud Map namespace"
  value       = aws_service_discovery_private_dns_namespace.atlas.arn
}

# Service discovery service outputs
output "user_service_discovery_arn" {
  description = "The ARN of the user service discovery service"
  value       = aws_service_discovery_service.user_service.arn
}

output "product_service_discovery_arn" {
  description = "The ARN of the product service discovery service"
  value       = aws_service_discovery_service.product_service.arn
}

output "order_service_discovery_arn" {
  description = "The ARN of the order service discovery service"
  value       = aws_service_discovery_service.order_service.arn
}

output "payment_service_discovery_arn" {
  description = "The ARN of the payment service discovery service"
  value       = aws_service_discovery_service.payment_service.arn
}

output "api_gateway_discovery_arn" {
  description = "The ARN of the api gateway service discovery service"
  value       = aws_service_discovery_service.api_gateway.arn
}

# Service DNS names for inter-service communication
output "user_service_dns" {
  description = "DNS name for user service"
  value       = "user-service.${aws_service_discovery_private_dns_namespace.atlas.name}"
}

output "product_service_dns" {
  description = "DNS name for product service"
  value       = "product-service.${aws_service_discovery_private_dns_namespace.atlas.name}"
}

output "order_service_dns" {
  description = "DNS name for order service"
  value       = "order-service.${aws_service_discovery_private_dns_namespace.atlas.name}"
}

output "payment_service_dns" {
  description = "DNS name for payment service"
  value       = "payment-service.${aws_service_discovery_private_dns_namespace.atlas.name}"
}

output "api_gateway_dns" {
  description = "DNS name for api gateway"
  value       = "api-gateway.${aws_service_discovery_private_dns_namespace.atlas.name}"
}
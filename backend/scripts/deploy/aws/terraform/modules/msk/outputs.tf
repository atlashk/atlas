# MSK Cluster outputs
output "cluster_arn" {
  description = "ARN of the MSK cluster"
  value       = aws_msk_cluster.main.arn
}

output "cluster_name" {
  description = "Name of the MSK cluster"
  value       = aws_msk_cluster.main.cluster_name
}

output "bootstrap_brokers" {
  description = "Bootstrap brokers for the MSK cluster"
  value       = aws_msk_cluster.main.bootstrap_brokers
}

output "bootstrap_brokers_sasl_iam" {
  description = "Bootstrap brokers for SASL/IAM authentication"
  value       = aws_msk_cluster.main.bootstrap_brokers_sasl_iam
}

output "bootstrap_brokers_tls" {
  description = "Bootstrap brokers for TLS authentication"
  value       = aws_msk_cluster.main.bootstrap_brokers_tls
}

output "zookeeper_connect_string" {
  description = "Zookeeper connection string"
  value       = aws_msk_cluster.main.zookeeper_connect_string
}

output "msk_client_role_arn" {
  description = "ARN of the IAM role for MSK client access"
  value       = aws_iam_role.msk_client_role.arn
}

output "msk_client_policy_arn" {
  description = "ARN of the IAM policy for MSK client access"
  value       = aws_iam_policy.msk_client_policy.arn
}

# Topic names that will be used by applications
output "kafka_topics" {
  description = "Kafka topic names for different event types"
  value = {
    user_events                              = "user-events"
    product_events                          = "product-events"
    order_events                            = "order-events"
    payment_events                          = "payment-events"
    saga_checkout_command_order             = "saga-checkout-command-order"
    saga_checkout_compensation_order        = "saga-checkout-compensation-order"
    saga_checkout_commandreply              = "saga-checkout-commandreply"
    saga_checkout_compensationreply         = "saga-checkout-compensationreply"
    saga_checkout_command_payment           = "saga-checkout-command-payment"
    saga_checkout_compensation_payment      = "saga-checkout-compensation-payment"
    saga_checkout_command_product           = "saga-checkout-command-product"
    saga_checkout_compensation_product      = "saga-checkout-compensation-product"
  }
}
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

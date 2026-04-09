# ==============================================================
# IAM Role for EBS CSI Driver (IRSA)
# ==============================================================
# Allows the EBS CSI Driver to create/delete EBS volumes on AWS.
# Uses IRSA instead of attaching the policy to the node IAM role
# (more secure — least-privilege per workload).
# ==============================================================

module "ebs_csi_irsa" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.0"

  role_name             = "${local.cluster_name}-ebs-csi-driver"
  attach_ebs_csi_policy = true

  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["kube-system:ebs-csi-controller-sa"]
    }
  }

  tags = local.common_tags
}

# ==============================================================
# IAM Role for AWS Load Balancer Controller (IRSA)
# ==============================================================
# Allows the controller to create and manage ALBs on AWS.
# Uses IRSA — least-privilege per workload, no node-level credentials.
# ==============================================================

module "aws_load_balancer_controller_irsa" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.0"

  role_name                              = "${local.cluster_name}-aws-load-balancer-controller"
  attach_load_balancer_controller_policy = true

  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["kube-system:aws-load-balancer-controller"]
    }
  }

  tags = local.common_tags
}

# ==============================================================
# AWS Load Balancer Controller — installed via Helm
# ==============================================================
# Reads Kubernetes Ingress resources (ingressClassName: alb) and
# provisions an ALB on AWS. Uses the IRSA role above to call AWS
# APIs without node-level credentials.
# ==============================================================

resource "helm_release" "aws_load_balancer_controller" {
  name       = "aws-load-balancer-controller"
  repository = "https://aws.github.io/eks-charts"
  chart      = "aws-load-balancer-controller"
  namespace  = "kube-system"
  version    = "~> 1.8"

  # Required: tell the controller which cluster to manage
  set {
    name  = "clusterName"
    value = module.eks.cluster_name
  }

  # Create the ServiceAccount and annotate it with the IRSA role
  set {
    name  = "serviceAccount.create"
    value = "true"
  }
  set {
    name  = "serviceAccount.name"
    value = "aws-load-balancer-controller"
  }
  set {
    name  = "serviceAccount.annotations.eks\\.amazonaws\\.com/role-arn"
    value = module.aws_load_balancer_controller_irsa.iam_role_arn
  }

  # Scope the controller to the correct region and VPC
  set {
    name  = "region"
    value = var.aws_region
  }
  set {
    name  = "vpcId"
    value = module.vpc.vpc_id
  }

  depends_on = [module.eks]
}

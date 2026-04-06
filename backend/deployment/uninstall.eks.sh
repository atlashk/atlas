#!/usr/bin/env bash
# ==============================================================
# uninstall.eks.sh
#
# Tears down the full Atlas stack on AWS EKS in reverse order:
#   1. helm uninstall     — removes all Kubernetes resources
#   2. terraform destroy  — destroys the EKS cluster + VPC +
#                           IAM roles + node groups
#   3. terraform destroy  — destroys the S3 bucket
#                           table for Terraform remote state
#
# Usage:
#   chmod +x uninstall.eks.sh
#   ./uninstall.eks.sh
#
# Options:
#   --skip-helm        Skip the Helm uninstall step
#   --skip-cluster     Skip the EKS cluster destroy step
#   --skip-bootstrap   Skip the bootstrap (S3) destroy step
#
# Prerequisites:
#   - terraform >= 1.9
#   - aws-cli   >= 2  (configured with appropriate credentials)
#   - helm      >= 3
#   - kubectl
#   - Both terraform.tfvars files must already exist:
#       terraform/eks/bootstrap/terraform.tfvars
#       terraform/eks/cluster/terraform.tfvars
# ==============================================================

set -euo pipefail

# ---------------------------------------------------------------
# Colour helpers
# ---------------------------------------------------------------
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
RESET='\033[0m'

log_info()    { echo -e "${CYAN}[INFO]${RESET}  $*"; }
log_success() { echo -e "${GREEN}[OK]${RESET}    $*"; }
log_warn()    { echo -e "${YELLOW}[WARN]${RESET}  $*"; }
log_error()   { echo -e "${RED}[ERROR]${RESET} $*" >&2; }
log_step()    { echo -e "\n${BOLD}${CYAN}===> $*${RESET}"; }

# ---------------------------------------------------------------
# Resolve directories relative to this script
# ---------------------------------------------------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TF_BOOTSTRAP_DIR="${SCRIPT_DIR}/terraform/eks/bootstrap"
TF_CLUSTER_DIR="${SCRIPT_DIR}/terraform/eks/cluster"
HELM_CHART_DIR="${SCRIPT_DIR}/helm"

HELM_RELEASE_NAME="atlas"
HELM_NAMESPACE="atlas"

# ---------------------------------------------------------------
# Parse flags
# ---------------------------------------------------------------
SKIP_HELM=false
SKIP_CLUSTER=false
SKIP_BOOTSTRAP=false

for arg in "$@"; do
    case "$arg" in
        --skip-helm)      SKIP_HELM=true ;;
        --skip-cluster)   SKIP_CLUSTER=true ;;
        --skip-bootstrap) SKIP_BOOTSTRAP=true ;;
        *)
            log_error "Unknown argument: $arg"
            echo "Usage: $0 [--skip-helm] [--skip-cluster] [--skip-bootstrap]"
            exit 1
            ;;
    esac
done

# ---------------------------------------------------------------
# Helper: check required tools
# ---------------------------------------------------------------
check_prerequisites() {
    log_step "Checking prerequisites"

    local missing=0
    for tool in terraform aws helm kubectl; do
        if command -v "$tool" &>/dev/null; then
            log_success "$tool found"
        else
            log_error "$tool is not installed or not on PATH"
            missing=$((missing + 1))
        fi
    done

    if [[ $missing -gt 0 ]]; then
        log_error "Please install the missing tools and re-run the script."
        exit 1
    fi
}

# ---------------------------------------------------------------
# Helper: assert that terraform.tfvars exists in a directory
# ---------------------------------------------------------------
assert_tfvars() {
    local dir="$1"
    if [[ ! -f "${dir}/terraform.tfvars" ]]; then
        log_error "terraform.tfvars not found in ${dir}"
        log_error "Copy the example file and fill in your values:"
        log_error "  cp ${dir}/terraform.tfvars.example ${dir}/terraform.tfvars"
        exit 1
    fi
}

# ---------------------------------------------------------------
# Helper: read bootstrap outputs (bucket + region)
# ---------------------------------------------------------------
read_bootstrap_outputs() {
    pushd "${TF_BOOTSTRAP_DIR}" > /dev/null

    log_info "Reading bootstrap outputs..."

    # terraform output -raw prints warning messages to stdout (not stderr) when
    # the state has no outputs, so sanitize the same way as read_cluster_outputs.
    local raw_bucket raw_region
    raw_bucket=$(terraform output -raw state_bucket_name 2>/dev/null || true)
    raw_region=$(terraform output -raw state_bucket_region 2>/dev/null || true)

    [[ "${raw_bucket}" == *"╷"* || "${raw_bucket}" == *"│"* ]] && raw_bucket=""
    [[ "${raw_region}" == *"╷"* || "${raw_region}" == *"│"* ]] && raw_region=""

    TF_BOOTSTRAP_BUCKET="${raw_bucket}"
    TF_BOOTSTRAP_REGION="${raw_region}"

    popd > /dev/null

    if [[ -z "${TF_BOOTSTRAP_BUCKET}" || -z "${TF_BOOTSTRAP_REGION}" ]]; then
        log_error "Could not read bootstrap outputs. Has the bootstrap been applied?"
        exit 1
    fi

    log_info "Remote state bucket : ${TF_BOOTSTRAP_BUCKET}"
    log_info "Remote state region : ${TF_BOOTSTRAP_REGION}"
}

# ---------------------------------------------------------------
# Helper: read cluster outputs (name + kubectl command)
# ---------------------------------------------------------------
read_cluster_outputs() {
    pushd "${TF_CLUSTER_DIR}" > /dev/null

    log_info "Reading cluster outputs..."

    # terraform output -raw prints warning messages to stdout (not stderr) when
    # the state has no outputs, so we must discard lines containing Terraform's
    # box-drawing warning characters rather than relying on the exit code alone.
    local raw_name raw_kubectl
    raw_name=$(terraform output -raw cluster_name 2>/dev/null || true)
    raw_kubectl=$(terraform output -raw configure_kubectl 2>/dev/null || true)

    [[ "${raw_name}"    == *"╷"* || "${raw_name}"    == *"│"* ]] && raw_name=""
    [[ "${raw_kubectl}" == *"╷"* || "${raw_kubectl}" == *"│"* ]] && raw_kubectl=""

    TF_CLUSTER_NAME="${raw_name}"
    TF_CONFIGURE_KUBECTL="${raw_kubectl}"

    popd > /dev/null

    if [[ -z "${TF_CLUSTER_NAME}" ]]; then
        log_warn "Could not read cluster name from Terraform outputs — cluster may not be deployed."
    else
        log_info "Cluster name: ${TF_CLUSTER_NAME}"
    fi
}

# ---------------------------------------------------------------
# Optional: configure kubectl before Helm uninstall
# ---------------------------------------------------------------
configure_kubectl() {
    if [[ -n "${TF_CONFIGURE_KUBECTL:-}" ]]; then
        log_step "Configuring kubectl"
        log_info "Running: ${TF_CONFIGURE_KUBECTL}"
        eval "${TF_CONFIGURE_KUBECTL}"
        log_success "kubectl context updated"
    else
        log_warn "kubectl configure command not available — skipping context update"
    fi
}

# ---------------------------------------------------------------
# Step 1 — Helm uninstall
# ---------------------------------------------------------------
run_helm_uninstall() {
    if [[ "${SKIP_HELM}" == "true" ]]; then
        log_warn "Skipping Helm uninstall (--skip-helm)"
        return
    fi

    log_step "Step 1/3 — Helm uninstall (${HELM_RELEASE_NAME})"

    if ! helm status "${HELM_RELEASE_NAME}" --namespace "${HELM_NAMESPACE}" &>/dev/null; then
        log_warn "Helm release '${HELM_RELEASE_NAME}' not found in namespace '${HELM_NAMESPACE}' — skipping"
    else
        log_info "helm uninstall ${HELM_RELEASE_NAME} --namespace ${HELM_NAMESPACE}"
        helm uninstall "${HELM_RELEASE_NAME}" \
            --namespace "${HELM_NAMESPACE}" \
            --wait \
            --timeout 15m
        log_success "Helm release '${HELM_RELEASE_NAME}' removed"
    fi

    # Remove the namespace if it exists
    if kubectl get namespace "${HELM_NAMESPACE}" &>/dev/null; then
        log_info "Deleting namespace '${HELM_NAMESPACE}'"
        kubectl delete namespace "${HELM_NAMESPACE}" --wait=true
        log_success "Namespace '${HELM_NAMESPACE}' deleted"
    fi
}

# ---------------------------------------------------------------
# Step 2 — Cluster destroy: EKS + VPC + node groups
# ---------------------------------------------------------------
run_cluster_destroy() {
    if [[ "${SKIP_CLUSTER}" == "true" ]]; then
        log_warn "Skipping cluster destroy (--skip-cluster)"
        return
    fi

    log_step "Step 2/3 — Terraform destroy (EKS cluster)"

    assert_tfvars "${TF_CLUSTER_DIR}"

    pushd "${TF_CLUSTER_DIR}" > /dev/null

    log_info "terraform init"
    terraform init -input=false -migrate-state -force-copy \
        -backend-config="bucket=${TF_BOOTSTRAP_BUCKET}" \
        -backend-config="key=${HELM_RELEASE_NAME}/eks/terraform.tfstate" \
        -backend-config="region=${TF_BOOTSTRAP_REGION}" \
        -backend-config="use_lockfile=true" \
        -backend-config="encrypt=true"

    log_info "terraform destroy"
    terraform destroy -input=false -auto-approve

    popd > /dev/null

    log_success "EKS cluster destroyed"
}

# ---------------------------------------------------------------
# Step 3 — Bootstrap destroy: S3 bucket
# ---------------------------------------------------------------
run_bootstrap_destroy() {
    if [[ "${SKIP_BOOTSTRAP}" == "true" ]]; then
        log_warn "Skipping bootstrap destroy (--skip-bootstrap)"
        return
    fi

    log_step "Step 3/3 — Terraform destroy (bootstrap)"

    assert_tfvars "${TF_BOOTSTRAP_DIR}"

    pushd "${TF_BOOTSTRAP_DIR}" > /dev/null

    log_info "terraform init"
    terraform init -input=false

    # S3 versioning is enabled on the bucket, so we must delete all object
    # versions and delete markers — not just current objects — before Terraform
    # can remove the bucket.
    log_warn "Emptying versioned bucket: ${TF_BOOTSTRAP_BUCKET} (region: ${TF_BOOTSTRAP_REGION})"

    local versions
    while true; do
        versions=$(aws s3api list-object-versions \
            --bucket "${TF_BOOTSTRAP_BUCKET}" \
            --region "${TF_BOOTSTRAP_REGION}" \
            --query '{Objects: Versions[].{Key:Key,VersionId:VersionId}}' \
            --output json 2>/dev/null || echo '{"Objects":[]}')

        local count
        count=$(echo "${versions}" | python3 -c "import sys,json; d=json.load(sys.stdin); print(len(d.get('Objects') or []))")
        [[ "${count}" -eq 0 ]] && break

        log_info "Deleting ${count} object version(s)..."
        echo "${versions}" | aws s3api delete-objects \
            --bucket "${TF_BOOTSTRAP_BUCKET}" \
            --region "${TF_BOOTSTRAP_REGION}" \
            --delete "$(cat)" > /dev/null
    done

    while true; do
        versions=$(aws s3api list-object-versions \
            --bucket "${TF_BOOTSTRAP_BUCKET}" \
            --region "${TF_BOOTSTRAP_REGION}" \
            --query '{Objects: DeleteMarkers[].{Key:Key,VersionId:VersionId}}' \
            --output json 2>/dev/null || echo '{"Objects":[]}')

        local count
        count=$(echo "${versions}" | python3 -c "import sys,json; d=json.load(sys.stdin); print(len(d.get('Objects') or []))")
        [[ "${count}" -eq 0 ]] && break

        log_info "Deleting ${count} delete marker(s)..."
        echo "${versions}" | aws s3api delete-objects \
            --bucket "${TF_BOOTSTRAP_BUCKET}" \
            --region "${TF_BOOTSTRAP_REGION}" \
            --delete "$(cat)" > /dev/null
    done

    log_success "Bucket emptied (all versions and delete markers removed)"

    log_info "terraform destroy"
    terraform destroy -input=false -auto-approve

    popd > /dev/null

    log_success "Bootstrap resources destroyed"
}

# ---------------------------------------------------------------
# Main
# ---------------------------------------------------------------
main() {
    echo -e "${BOLD}${RED}"
    echo "=============================================================="
    echo "  Atlas — EKS Full-Stack Uninstaller"
    echo "=============================================================="
    echo -e "${RESET}"

    check_prerequisites

    log_warn "This will PERMANENTLY destroy the following resources:"
    [[ "${SKIP_HELM}"      == "false" ]] && echo "    • Helm release '${HELM_RELEASE_NAME}' and namespace '${HELM_NAMESPACE}'"
    [[ "${SKIP_CLUSTER}"   == "false" ]] && echo "    • EKS cluster, VPC, IAM roles, and node groups (Terraform)"
    [[ "${SKIP_BOOTSTRAP}" == "false" ]] && echo "    • S3 remote-state bucket (Terraform)"
    echo ""

    # Read infrastructure outputs before starting destruction
    assert_tfvars "${TF_BOOTSTRAP_DIR}"
    log_step "Reading Terraform state outputs"
    terraform -chdir="${TF_BOOTSTRAP_DIR}" init -input=false -reconfigure &>/dev/null || true
    read_bootstrap_outputs

    if [[ "${SKIP_CLUSTER}" == "false" ]]; then
        assert_tfvars "${TF_CLUSTER_DIR}"
        terraform -chdir="${TF_CLUSTER_DIR}" init -input=false -reconfigure \
            -backend-config="bucket=${TF_BOOTSTRAP_BUCKET}" \
            -backend-config="key=${HELM_RELEASE_NAME}/eks/terraform.tfstate" \
            -backend-config="region=${TF_BOOTSTRAP_REGION}" \
            -backend-config="use_lockfile=true" \
            -backend-config="encrypt=true" &>/dev/null || true
        read_cluster_outputs
        configure_kubectl
    fi

    run_helm_uninstall
    run_cluster_destroy
    run_bootstrap_destroy

    echo -e "\n${BOLD}${GREEN}"
    echo "=============================================================="
    echo "  Uninstall complete!"
    echo "  All Atlas EKS resources have been removed."
    echo "=============================================================="
    echo -e "${RESET}"
}

main "$@"

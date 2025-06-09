# Atlas Complete Deployment Script (PowerShell)
# Orchestrates the deployment of the entire Atlas platform

param(
    [switch]$SkipBuild,
    [switch]$SkipObservability,
    [switch]$SkipHealthCheck,
    [switch]$AutoBuild,
    [switch]$Help
)

# Configuration
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$K8sRoot = Split-Path -Parent $ScriptDir
$BaseDir = Join-Path $K8sRoot "base"

# Colors for output (Windows PowerShell compatible)
function Write-Log { param($Message) Write-Host "[INFO] $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') - $Message" -ForegroundColor Green }
function Write-Warn { param($Message) Write-Host "[WARN] $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') - $Message" -ForegroundColor Yellow }
function Write-Error { param($Message) Write-Host "[ERROR] $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') - $Message" -ForegroundColor Red }
function Write-Header { param($Message) Write-Host "[ATLAS] $Message" -ForegroundColor Magenta }

# Function to print banner
function Show-Banner {
    Write-Host ""
    Write-Host "╔══════════════════════════════════════════════════════════════╗" -ForegroundColor Magenta
    Write-Host "║                        ATLAS PLATFORM                       ║" -ForegroundColor Magenta
    Write-Host "║                   Kubernetes Deployment                     ║" -ForegroundColor Magenta
    Write-Host "╚══════════════════════════════════════════════════════════════╝" -ForegroundColor Magenta
    Write-Host ""
}

# Function to check prerequisites
function Test-Prerequisites {
    Write-Header "Checking prerequisites..."
    
    $RequiredTools = @("kubectl", "docker", "minikube")
    $MissingTools = @()
    
    foreach ($Tool in $RequiredTools) {
        if (!(Get-Command $Tool -ErrorAction SilentlyContinue)) {
            $MissingTools += $Tool
        }
    }
    
    if ($MissingTools.Count -gt 0) {
        Write-Error "Missing required tools: $($MissingTools -join ', ')"
        Write-Host ""
        Write-Host "Please install the missing tools:"
        Write-Host "  - kubectl: https://kubernetes.io/docs/tasks/tools/"
        Write-Host "  - docker: https://docs.docker.com/get-docker/"
        Write-Host "  - minikube: https://minikube.sigs.k8s.io/docs/start/"
        exit 1
    }
    
    # Check if minikube is running
    try {
        $null = minikube status 2>$null
    } catch {
        Write-Warn "Minikube is not running. Starting minikube..."
        minikube start --cpus=4 --memory=8g
    }
    
    Write-Log "Prerequisites check passed"
}

# Function to deploy infrastructure
function Deploy-Infrastructure {
    Write-Header "Deploying infrastructure services..."
    
    $Components = @("mysql", "redis", "kafka", "rabbitmq", "keycloak")
    
    foreach ($Component in $Components) {
        $ComponentDir = Join-Path $BaseDir "infrastructure\$Component"
        
        if (Test-Path $ComponentDir) {
            Write-Log "Deploying infrastructure/$Component..."
            
            if (Test-Path (Join-Path $ComponentDir "kustomization.yaml")) {
                kubectl apply -k $ComponentDir
            } else {
                kubectl apply -f $ComponentDir\
            }
            
            Write-Log "$Component deployed successfully"
        } else {
            Write-Warn "Component directory $ComponentDir does not exist, skipping"
        }
    }
}

# Function to deploy services
function Deploy-Services {
    Write-Header "Deploying application services..."
    
    # All microservices (including auth and gateway)
    $AllServices = @("auth-server", "api-gateway", "user-service", "product-service", "order-service", "notification-service")
    foreach ($Service in $AllServices) {
        $ServiceDir = Join-Path $BaseDir "microservices\$Service"
        if (Test-Path $ServiceDir) {
            Write-Log "Deploying microservices/$Service..."
            kubectl apply -k $ServiceDir
        }
    }
}

# Function to wait for deployments
function Wait-ForDeployments {
    Write-Log "Waiting for all deployments to be ready..."
    
    $Deployments = @("mysql", "redis", "kafka", "user-service", "product-service", "order-service", "notification-service")
    
    foreach ($Deployment in $Deployments) {
        Write-Log "Waiting for $Deployment..."
        kubectl wait --for=condition=available --timeout=300s deployment/$Deployment
    }
}

# Function to print summary
function Show-Summary {
    Write-Header "Deployment Summary"
    Write-Host ""
    Write-Log "Atlas platform deployed successfully!"
    Write-Host ""
    Write-Host "Application Endpoints:" -ForegroundColor Blue
    Write-Host "  🌐 API Gateway:      http://localhost:8080"
    Write-Host "  🔐 Auth Server:      http://localhost:8091"
    Write-Host "  👥 User Service:     http://localhost:8081"
    Write-Host "  📦 Product Service:  http://localhost:8082"
    Write-Host "  🛒 Order Service:    http://localhost:8083"
    Write-Host "  📧 Notification:     http://localhost:8084"
    Write-Host ""
    Write-Host "Useful Commands:" -ForegroundColor Blue
    Write-Host "  kubectl get all                           # View all resources"
    Write-Host "  kubectl get pods --watch                  # Watch pod status"
    Write-Host "  kubectl logs -f deployment/user-service   # View service logs"
    Write-Host "  minikube dashboard                        # Open K8s dashboard"
    Write-Host ""
}

# Show help
if ($Help) {
    Write-Host "Usage: .\deploy-all.ps1 [OPTIONS]"
    Write-Host ""
    Write-Host "Options:"
    Write-Host "  -SkipBuild           Skip Docker image building"
    Write-Host "  -SkipObservability   Skip observability deployment"
    Write-Host "  -SkipHealthCheck     Skip health checks"
    Write-Host "  -AutoBuild           Attempt to auto-build missing images"
    Write-Host "  -Help                Show this help message"
    Write-Host ""
    exit 0
}

# Main execution
try {
    Show-Banner
    
    $StartTime = Get-Date
    
    Test-Prerequisites
    Deploy-Infrastructure
    Deploy-Services
    Wait-ForDeployments
    
    $EndTime = Get-Date
    $Duration = ($EndTime - $StartTime).TotalSeconds
    
    Write-Host ""
    Write-Log "Total deployment time: $([math]::Round($Duration, 2)) seconds"
    
    Show-Summary
} catch {
    Write-Error "Deployment failed: $($_.Exception.Message)"
    exit 1
} 
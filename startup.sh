#!/bin/bash

# Load logger
source "$(dirname "$0")/backend/scripts/utils/logger.sh"

# Default options
SKIP_BUILD=false

# Show usage if help is requested
if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
    log_info "Usage: $0 [OPTIONS]"
    log_info ""
    log_info "Atlas Platform Startup Script - Starts the complete Atlas platform"
    log_info ""
    log_info "Options:"
    log_info "  --skip-build        Skip all build steps (backend JAR, Docker images)"
    log_info "  -h, --help          Show this help message"
    log_info ""
    log_info "Examples:"
    log_info "  $0                  # Start with builds"
    log_info "  $0 --skip-build     # Start without builds"
    exit 0
fi

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-build)
            SKIP_BUILD=true
            shift
            ;;
        *)
            log_error "Unknown option: $1"
            log_info "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Check Node.js
if ! command -v node &> /dev/null; then
    log_error "Node.js is not installed. Please install Node.js 22 or later."
    exit 1
else
    node_version=$(node --version | cut -d'v' -f2)  # Remove 'v' prefix
    major_version=$(echo $node_version | cut -d'.' -f1)
    
    if [ "$major_version" -lt 22 ]; then
        log_error "Node.js version $node_version is not supported. Please install Node.js 22 or later."
        exit 1
    fi
    log_success "Node.js found: v$node_version"
fi

# Start backend services using Docker Compose
log_info "Starting backend services..."
if [[ "$SKIP_BUILD" == true ]]; then
    if ! "$(dirname "$0")/backend/scripts/deploy/onprem/compose/compose-start.sh" --skip-build; then
        log_error "Backend services failed to start. Exiting..."
        exit 1
    fi
else
    if ! "$(dirname "$0")/backend/scripts/deploy/onprem/compose/compose-start.sh"; then
        log_error "Backend services failed to start. Exiting..."
        exit 1
    fi
fi

log_success "Backend services started successfully"

# Wait for API Gateway to be ready by checking health endpoint
log_info "Waiting for API Gateway to be ready..."
api_gateway_url="http://localhost:8080"
health_endpoint="${api_gateway_url}/actuator/health"
max_attempts=60
attempt=0

while [ $attempt -lt $max_attempts ]; do
    attempt=$((attempt + 1))
    log_progress "Checking API Gateway health (attempt $attempt/$max_attempts)..."
    
    # Check if API Gateway health endpoint is responding
    if curl -s -f "$health_endpoint" > /dev/null 2>&1; then
        clear_progress
        log_success "API Gateway is ready and healthy!"
        break
    fi
    
    if [ $attempt -eq $max_attempts ]; then
        clear_progress
        log_error "API Gateway failed to become ready after $max_attempts attempts"
        log_error "Please check the backend services status"
        exit 1
    fi
    
    sleep 2
done

# Start frontend in development mode
log_info "Starting frontend in development mode..."
cd "$(dirname "$0")/frontend"

# Check if node_modules exists, if not install dependencies
if [ ! -d "node_modules" ]; then
    log_info "Installing frontend dependencies..."
    npm install
fi

# Start the frontend development server
npm run dev &
FRONTEND_PID=$!

# Function to stop services on exit
stop_services() {
    log_info "Shutting down services..."

    if [ ! -z "$FRONTEND_PID" ]; then
        kill $FRONTEND_PID 2>/dev/null || true
    fi

    # Stop backend services using compose-stop script
    "$(dirname "$0")/backend/scripts/deploy/onprem/compose/compose-stop.sh" 2>/dev/null || true

    exit 0
}

# Set up signal handlers
trap stop_services SIGINT SIGTERM

log_info ""
log_info "==================================="
log_info "Atlas Platform Started Successfully!"
log_info "==================================="
log_info "Backend Services:"
log_info "  - API Gateway: http://localhost:8080"
log_info "  - Prometheus: http://localhost:9090"
log_info "  - Grafana: http://localhost:3000"
log_info "  - Zipkin: http://localhost:9411"
log_info ""
log_info "Frontend (Development):"
log_info "  - Web App: http://localhost:9000"
log_info ""
log_info "Press Ctrl+C to stop all services"
log_info "==================================="

# Wait for frontend process
wait $FRONTEND_PID

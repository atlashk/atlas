#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../.." && pwd)"

# Source logger
source "$PROJECT_ROOT/scripts/logger.sh"

# Check Docker Compose prerequisites
check_docker_compose_prerequisites() {
    if ! docker info > /dev/null 2>&1; then
        log_error "Docker is not running. Please start Docker and try again."
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose is not installed"
        exit 1
    fi
}

# Create volumes for services
create_volumes() {
    local compose_file=$1
    local project_prefix=$2
    
    log_info "Creating volumes for services in $compose_file"
    
    # Get all volume names from the volumes section
    local volumes
    volumes=$(docker-compose -f "$compose_file" config --format json | jq -r '.volumes | keys[]' 2>/dev/null || true)
    
    if [ -n "$volumes" ]; then
        for volume in $volumes; do
            local full_volume_name="${project_prefix}_${volume}"
            if ! docker volume inspect "$full_volume_name" >/dev/null 2>&1; then
                log_info "Creating volume: $full_volume_name"
                docker volume create "$full_volume_name"
            fi
        done
    fi
}

# Check service health
check_service_health() {
    local service=$1
    shift
    local compose_files=("$@")
    
    log_info "Checking health of service: $service"
    if ! docker-compose "${compose_files[@]}" -p "$PROJECT_NAME" ps "$service" | grep -q "Up"; then
        log_error "Service $service is not running."
        return 1
    fi
    log_success "Service $service is running."
    return 0
}

# Wait for services to be ready with health checks
wait_for_services_ready() {
    local stack_name=$1
    local compose_file=$2
    shift 2
    local services=("$@")
    
    log_info "Waiting for services to be ready: ${services[*]}"
    
    for service in "${services[@]}"; do
        local max_attempts=60  # 5 minutes max (60 * 5 seconds)
        local attempt=1
        
        log_info "Waiting for $service to be ready..."
        
        while [ $attempt -le $max_attempts ]; do
            # Check if service is running and healthy
            if docker-compose -f "$compose_file" -p "$stack_name" ps "$service" | grep -q "Up"; then
                # Additional health checks based on service type
                case "$service" in
                    mysql)
                        if docker-compose -f "$compose_file" -p "$stack_name" exec -T "$service" mysqladmin ping -h localhost --silent 2>/dev/null; then
                            log_success "$service is ready!"
                            break
                        fi
                        ;;
                    redis)
                        if docker-compose -f "$compose_file" -p "$stack_name" exec -T "$service" redis-cli ping 2>/dev/null | grep -q "PONG"; then
                            log_success "$service is ready!"
                            break
                        fi
                        ;;
                    kafka)
                        # Check if Kafka is accepting connections
                        if docker-compose -f "$compose_file" -p "$stack_name" exec -T "$service" kafka-broker-api-versions --bootstrap-server localhost:9092 >/dev/null 2>&1; then
                            log_success "$service is ready!"
                            break
                        fi
                        ;;
                    *)
                        # For other services, just check if they're up and responding
                        log_success "$service is running!"
                        break
                        ;;
                esac
            fi
            
            if [ $attempt -eq $max_attempts ]; then
                log_error "$service failed to become ready after $max_attempts attempts"
                return 1
            fi
            
            log_progress "Waiting for $service... (attempt $attempt/$max_attempts)"
            sleep 5
            ((attempt++))
        done
    done
    
    clear_progress
    log_success "All services are ready!"
    return 0
}

# Remove volumes for services
remove_volumes() {
    local compose_file=$1
    local project_prefix=$2
    
    log_info "Removing volumes for services in $compose_file"
    
    # Get all volume names from the volumes section
    local volumes
    volumes=$(docker-compose -f "$compose_file" config --format json | jq -r '.volumes | keys[]' 2>/dev/null || true)
    
    if [ -n "$volumes" ]; then
        for volume in $volumes; do
            local full_volume_name="${project_prefix}_${volume}"
            if docker volume inspect "$full_volume_name" >/dev/null 2>&1; then
                log_info "Removing volume: $full_volume_name"
                docker volume rm "$full_volume_name" 2>/dev/null || true
            fi
        done
    fi
} 
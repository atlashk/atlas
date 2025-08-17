#!/bin/bash

set -euo pipefail

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../../../../.." && pwd)"

# Load logger
source "$PROJECT_ROOT/backend/scripts/logger.sh"

ORDER_EXCHANGE="order_events"
QUEUES=(
  "order_events.order"
  "order_events.product"
  "order_events.user"
  "order_events.notification"
)

log_section "RabbitMQ Setup"

# Declare exchange
log_info "Declaring exchange: ${ORDER_EXCHANGE}"
rabbitmqadmin declare exchange name=${ORDER_EXCHANGE} type=fanout durable=true

# Declare queues and bindings
for queue in "${QUEUES[@]}"; do
  log_info "Declaring queue $queue and binding it to exchange $ORDER_EXCHANGE"
  rabbitmqadmin declare queue name="$queue" durable=true
  rabbitmqadmin declare binding source="$ORDER_EXCHANGE" destination="$queue"
done

log_success "RabbitMQ setup completed successfully!"

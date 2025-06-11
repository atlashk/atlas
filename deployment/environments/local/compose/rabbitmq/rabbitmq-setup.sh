#!/bin/bash

ORDER_EXCHANGE="order_events"
QUEUES=(
  "order_events.order"
  "order_events.product"
  "order_events.user"
  "order_events.notification"
  "order_events.report"
)

# Declare exchange
echo "Declaring exchange: ${ORDER_EXCHANGE}"
rabbitmqadmin declare exchange name=${ORDER_EXCHANGE} type=fanout durable=true

# Declare queues and bindings
for queue in "${QUEUES[@]}"; do
  echo "Declaring queue $queue and binding it to exchange $ORDER_EXCHANGE"
  rabbitmqadmin declare queue name="$queue" durable=true
  rabbitmqadmin declare binding source="$ORDER_EXCHANGE" destination="$queue"
done

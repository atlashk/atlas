# Base image with RabbitMQ and management plugin
FROM rabbitmq:3.13-management-alpine

# Install necessary tools
RUN apk update && apk add --no-cache curl

# Install the Delayed Message Exchange plugin
RUN curl -L https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases/download/v3.13.0/rabbitmq_delayed_message_exchange-3.13.0.ez > $RABBITMQ_HOME/plugins/rabbitmq_delayed_message_exchange-3.13.0.ez && \
    rabbitmq-plugins enable --offline rabbitmq_delayed_message_exchange

# Copy setup script to the container
COPY rabbitmq-setup.sh /docker-entrypoint-initdb.d/

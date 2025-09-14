package org.atlas.infrastructure.messaging.rabbitmq.core.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.infrastructure.messaging.rabbitmq.core.common.RabbitmqProps.BindingConfig;
import org.atlas.infrastructure.messaging.rabbitmq.core.common.RabbitmqProps.ExchangeConfig;
import org.atlas.infrastructure.messaging.rabbitmq.core.common.RabbitmqProps.QueueConfig;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RabbitmqResourcesInitializer implements InitializingBean {

  private final RabbitmqProps rabbitmqProps;
  private final AmqpAdmin amqpAdmin;

  @Override
  public void afterPropertiesSet() throws Exception {
    createExchanges();
    createQueues();
    createBindings();
  }

  private void createExchanges() {
    if (rabbitmqProps.getExchanges() == null) {
      return;
    }

    for (ExchangeConfig exchangeConfig : rabbitmqProps.getExchanges()) {
      Exchange exchange = createExchange(exchangeConfig);
      amqpAdmin.declareExchange(exchange);
      log.info("Declared exchange: {}", exchangeConfig.getName());
    }
  }

  private Exchange createExchange(ExchangeConfig config) {
    return switch (config.getType()) {
      case DIRECT -> new DirectExchange(
          config.getName(),
          config.isDurable(),
          config.isAutoDelete(),
          config.getArguments());
      case FANOUT -> new FanoutExchange(
          config.getName(),
          config.isDurable(),
          config.isAutoDelete(),
          config.getArguments());
      case TOPIC -> new TopicExchange(
          config.getName(),
          config.isDurable(),
          config.isAutoDelete(),
          config.getArguments());
      default ->
          throw new IllegalArgumentException("Unsupported exchange type: " + config.getType());
    };
  }

  private void createQueues() {
    if (rabbitmqProps.getQueues() == null) {
      return;
    }

    for (QueueConfig queueConfig : rabbitmqProps.getQueues()) {
      Queue queue = new Queue(
          queueConfig.getName(),
          queueConfig.isDurable(),
          queueConfig.isExclusive(),
          queueConfig.isAutoDelete(),
          queueConfig.getArguments());
      amqpAdmin.declareQueue(queue);
      log.info("Declared queue: {}", queueConfig.getName());
    }
  }

  private void createBindings() {
    if (rabbitmqProps.getBindings() == null) {
      return;
    }

    for (BindingConfig bindingConfig : rabbitmqProps.getBindings()) {
      Binding binding = new Binding(
          bindingConfig.getQueue(),
          DestinationType.QUEUE,
          bindingConfig.getExchange(),
          bindingConfig.getRoutingKey(),
          bindingConfig.getArguments());
      amqpAdmin.declareBinding(binding);
      log.info("Created binding between exchange: {} and queue: {} with routing key: {}",
          bindingConfig.getExchange(),
          bindingConfig.getQueue(),
          bindingConfig.getRoutingKey());
    }
  }
}

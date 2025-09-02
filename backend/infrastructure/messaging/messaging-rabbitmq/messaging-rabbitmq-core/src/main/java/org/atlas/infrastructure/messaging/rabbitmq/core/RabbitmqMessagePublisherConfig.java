package org.atlas.infrastructure.messaging.rabbitmq.core;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.json.jackson.JacksonService;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RabbitmqMessagePublisherConfig {

  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
      MessageConverter messageConverter) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

    // Set the publisher confirm callback.
    // Publisher confirms are a mechanism that allows you to be notified when the message is successfully delivered to the RabbitMQ broker (either successfully or failed)
    rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
      if (ack) {
        log.info("Message was delivered to the broker successfully: correlationId={}",
            getCorrelationId(correlationData));
      } else {
        log.error("Message was delivered to the broker failed: correlationId={}, cause={}",
            getCorrelationId(correlationData), cause);
      }
    });

    // Message converter
    rabbitTemplate.setMessageConverter(messageConverter);

    return rabbitTemplate;
  }

  @Bean
  public MessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter(JacksonService.OBJECT_MAPPER);
  }

  private String getCorrelationId(CorrelationData correlationData) {
    return Optional.ofNullable(correlationData)
        .map(CorrelationData::getId)
        .orElse(null);
  }
}

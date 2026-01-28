package org.atlas.common.infrastructure.messaging.rabbitmq.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.support.RetryTemplateBuilder;

@Configuration
@Slf4j
public class RabbitmqMessageConsumerConfig {

  @Bean
  public SimpleRabbitListenerContainerFactory customContainerFactory(RetryTemplate retryTemplate,
      ConnectionFactory connectionFactory,
      MessageConverter messageConverter) {
    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setMessageConverter(messageConverter);
    factory.setRetryTemplate(retryTemplate);
    return factory;
  }

  @Bean
  public RetryTemplate retryTemplate() {
    return new RetryTemplateBuilder()
        .customBackoff(new ExponentialBackOffPolicy() {{
          setInitialInterval(1000); // 1 second
          setMultiplier(2.0);       // Exponential multiplier
        }})
        .maxAttempts(4) // 1 attempt + 3 retries
        .retryOn(Exception.class) // Retry for all exceptions
        .build();
  }
}

package org.atlas.libs.messaging.rabbitmq.consumer;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;

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
    factory.setDefaultRequeueRejected(true);
    return factory;
  }

  @Bean
  public RetryTemplate retryTemplate() {
    RetryPolicy retryPolicy = RetryPolicy.builder()
        .maxRetries(3)
        .delay(Duration.ofSeconds(1))
        .multiplier(2.0)
        .includes(Exception.class)
        .excludes(ClassCastException.class)
        .build();
    return new RetryTemplate(retryPolicy);
  }
}

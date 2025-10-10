package org.atlas.infrastructure.messaging.sns.impl.payment.consumer;

import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.saga.command.SagaCommandHandlerDispatcher;
import org.atlas.framework.saga.messaging.payload.SagaCommand;
import org.atlas.infrastructure.messaging.sns.core.common.SnsProps;
import org.atlas.infrastructure.messaging.sns.core.consumer.BaseSnsMessageConsumer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;

@Component
@Slf4j
public class SnsSagaCheckoutCommandConsumer extends BaseSnsMessageConsumer
    implements InitializingBean {

  private final SagaCommandHandlerDispatcher dispatcher;

  public SnsSagaCheckoutCommandConsumer(SnsProps snsProps, SqsClient sqsClient,
      SagaCommandHandlerDispatcher dispatcher) {
    super(snsProps, sqsClient);
    this.dispatcher = dispatcher;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    final String queueName = "saga.checkout.command.payment";
    consumeMessages(queueName, snsProps.getSqsQueueUrl().get(queueName));
  }

  @Override
  protected void handleMessage(Object messagePayload) {
    dispatcher.dispatch((SagaCommand) messagePayload);
  }
}

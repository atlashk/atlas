package org.atlas.infrastructure.messaging.sns.impl.product.consumer;

import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.saga.compensation.SagaCompensationHandlerDispatcher;
import org.atlas.framework.saga.messaging.payload.SagaCompensation;
import org.atlas.infrastructure.messaging.sns.core.common.SnsProps;
import org.atlas.infrastructure.messaging.sns.core.consumer.BaseSnsMessageConsumer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;

@Component
@Slf4j
public class SnsSagaCheckoutCompensationConsumer extends BaseSnsMessageConsumer
    implements InitializingBean {

  private final SagaCompensationHandlerDispatcher dispatcher;

  public SnsSagaCheckoutCompensationConsumer(SnsProps snsProps, SqsClient sqsClient,
      SagaCompensationHandlerDispatcher dispatcher) {
    super(snsProps, sqsClient);
    this.dispatcher = dispatcher;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    final String queueName = "saga.checkout.compensation.product";
    consumeMessages(queueName, snsProps.getSqsQueueUrl().get(queueName));
  }

  @Override
  protected void handleMessage(Object messagePayload) {
    dispatcher.dispatch((SagaCompensation) messagePayload);
  }
}

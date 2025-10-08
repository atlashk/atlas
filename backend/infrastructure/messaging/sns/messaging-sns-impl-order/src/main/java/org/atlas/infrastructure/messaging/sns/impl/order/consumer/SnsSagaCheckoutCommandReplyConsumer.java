package org.atlas.infrastructure.messaging.sns.impl.order.consumer;

import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.saga.messaging.payload.SagaCommandReply;
import org.atlas.framework.saga.orchestrator.SagaOrchestrator;
import org.atlas.infrastructure.messaging.sns.core.common.SnsProps;
import org.atlas.infrastructure.messaging.sns.core.consumer.BaseSnsMessageConsumer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;

@Component
@Slf4j
public class SnsSagaCheckoutCommandReplyConsumer extends BaseSnsMessageConsumer
    implements InitializingBean {

  private final SagaOrchestrator sagaOrchestrator;

  public SnsSagaCheckoutCommandReplyConsumer(SnsProps snsProps, SqsClient sqsClient,
      SagaOrchestrator sagaOrchestrator) {
    super(snsProps, sqsClient);
    this.sagaOrchestrator = sagaOrchestrator;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    final String queueName = "saga.checkout.commandreply";
    consumeMessages(queueName, snsProps.getSqsQueueUrl().get(queueName));
  }

  @Override
  protected void handleMessage(Object messagePayload) {
    sagaOrchestrator.handleSagaCommandReply((SagaCommandReply) messagePayload);
  }
}

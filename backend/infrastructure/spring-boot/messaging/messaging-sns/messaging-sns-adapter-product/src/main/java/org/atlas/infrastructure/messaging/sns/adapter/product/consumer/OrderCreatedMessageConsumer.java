package org.atlas.infrastructure.messaging.sns.adapter.product.consumer;

import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.product.event.OrderCreatedEventHandler;
import org.atlas.framework.domain.event.contract.order.OrderCreatedEvent;
import org.atlas.infrastructure.messaging.sns.core.SnsMessageConsumer;
import org.atlas.infrastructure.messaging.sns.core.SnsProps;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;

@Component
@Slf4j
public class OrderCreatedMessageConsumer extends SnsMessageConsumer implements InitializingBean {

  private final OrderCreatedEventHandler orderCreatedEventHandler;

  public OrderCreatedMessageConsumer(SnsProps snsProps,
      SqsClient sqsClient,
      OrderCreatedEventHandler orderCreatedEventHandler) {
    super(snsProps, sqsClient);
    this.orderCreatedEventHandler = orderCreatedEventHandler;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    consumeMessages("order-created-event",
        snsProps.getSqsQueueUrl().getOrderCreatedEvent());
  }

  @Override
  protected void handleMessage(Object messagePayload) {
    orderCreatedEventHandler.handle((OrderCreatedEvent) messagePayload);
  }
}

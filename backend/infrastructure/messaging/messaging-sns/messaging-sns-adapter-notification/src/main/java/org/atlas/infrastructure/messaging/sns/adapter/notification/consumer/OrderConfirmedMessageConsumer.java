package org.atlas.infrastructure.messaging.sns.adapter.notification.consumer;

import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.notification.event.OrderConfirmedEventHandler;
import org.atlas.framework.domain.event.contract.order.OrderConfirmedEvent;
import org.atlas.infrastructure.messaging.sns.core.SnsMessageConsumer;
import org.atlas.infrastructure.messaging.sns.core.SnsProps;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;

@Component
@Slf4j
public class OrderConfirmedMessageConsumer extends SnsMessageConsumer implements InitializingBean {

  private final OrderConfirmedEventHandler orderConfirmedEventHandler;

  public OrderConfirmedMessageConsumer(SnsProps snsProps,
      SqsClient sqsClient,
      OrderConfirmedEventHandler orderConfirmedEventHandler) {
    super(snsProps, sqsClient);
    this.orderConfirmedEventHandler = orderConfirmedEventHandler;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    consumeMessages("order-confirmed-event",
        snsProps.getSqsQueueUrl().getOrderConfirmedEvent());
  }

  @Override
  protected void handleMessage(Object messagePayload) {
    orderConfirmedEventHandler.handle((OrderConfirmedEvent) messagePayload);
  }
}

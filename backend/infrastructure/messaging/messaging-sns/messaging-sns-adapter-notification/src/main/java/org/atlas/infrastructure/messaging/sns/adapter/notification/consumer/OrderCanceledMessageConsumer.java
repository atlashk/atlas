package org.atlas.infrastructure.messaging.sns.adapter.notification.consumer;

import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.notification.event.OrderCanceledEventHandler;
import org.atlas.framework.domain.event.contract.order.OrderCanceledEvent;
import org.atlas.infrastructure.messaging.sns.core.SnsMessageConsumer;
import org.atlas.infrastructure.messaging.sns.core.SnsProps;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;

@Component
@Slf4j
public class OrderCanceledMessageConsumer extends SnsMessageConsumer implements InitializingBean {

  private final OrderCanceledEventHandler orderCanceledEventHandler;

  public OrderCanceledMessageConsumer(SnsProps snsProps,
      SqsClient sqsClient,
      OrderCanceledEventHandler orderCanceledEventHandler) {
    super(snsProps, sqsClient);
    this.orderCanceledEventHandler = orderCanceledEventHandler;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    consumeMessages("order-canceled-event",
        snsProps.getSqsQueueUrl().getOrderCanceledEvent());
  }

  @Override
  protected void handleMessage(Object messagePayload) {
    orderCanceledEventHandler.handle((OrderCanceledEvent) messagePayload);
  }
}

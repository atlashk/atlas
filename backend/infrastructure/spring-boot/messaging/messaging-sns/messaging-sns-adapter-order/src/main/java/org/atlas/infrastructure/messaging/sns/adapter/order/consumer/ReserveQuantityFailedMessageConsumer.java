package org.atlas.infrastructure.messaging.sns.adapter.order.consumer;

import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.order.event.ReserveQuantityFailedEventHandler;
import org.atlas.framework.domain.event.contract.product.ReserveQuantityFailedEvent;
import org.atlas.infrastructure.messaging.sns.core.SnsMessageConsumer;
import org.atlas.infrastructure.messaging.sns.core.SnsProps;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;

@Component
@Slf4j
public class ReserveQuantityFailedMessageConsumer extends SnsMessageConsumer
    implements InitializingBean {

  private final ReserveQuantityFailedEventHandler reserveQuantityFailedEventHandler;

  public ReserveQuantityFailedMessageConsumer(SnsProps snsProps,
      SqsClient sqsClient,
      ReserveQuantityFailedEventHandler reserveQuantityFailedEventHandler) {
    super(snsProps, sqsClient);
    this.reserveQuantityFailedEventHandler = reserveQuantityFailedEventHandler;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    consumeMessages("reserve-quantity-failed-event",
        snsProps.getSqsQueueUrl().getReserveQuantityFailedEvent());
  }

  @Override
  protected void handleMessage(Object messagePayload) {
    reserveQuantityFailedEventHandler.handle((ReserveQuantityFailedEvent) messagePayload);
  }
}

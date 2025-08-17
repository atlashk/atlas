package org.atlas.infrastructure.messaging.sns.adapter.order.consumer;

import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.order.event.ReserveQuantitySucceededEventHandler;
import org.atlas.framework.domain.event.contract.product.ReserveQuantitySucceededEvent;
import org.atlas.infrastructure.messaging.sns.core.SnsMessageConsumer;
import org.atlas.infrastructure.messaging.sns.core.SnsProps;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;

@Component
@Slf4j
public class ReserveQuantitySucceededMessageConsumer extends SnsMessageConsumer
    implements InitializingBean {

  private final ReserveQuantitySucceededEventHandler reserveQuantitySucceededEventHandler;

  public ReserveQuantitySucceededMessageConsumer(SnsProps snsProps,
      SqsClient sqsClient,
      ReserveQuantitySucceededEventHandler reserveQuantitySucceededEventHandler) {
    super(snsProps, sqsClient);
    this.reserveQuantitySucceededEventHandler = reserveQuantitySucceededEventHandler;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    consumeMessages("reserve-quantity-succeeded-event",
        snsProps.getSqsQueueUrl().getReserveQuantitySucceededEvent());
  }

  @Override
  protected void handleMessage(Object messagePayload) {
    reserveQuantitySucceededEventHandler.handle((ReserveQuantitySucceededEvent) messagePayload);
  }
}

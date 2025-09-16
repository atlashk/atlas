package org.atlas.infrastructure.messaging.external.sns.impl.product;

import lombok.extern.slf4j.Slf4j;
import org.atlas.infrastructure.domain.event.handler.DomainEventDispatcher;
import org.atlas.infrastructure.messaging.external.sns.core.common.SnsConstant;
import org.atlas.infrastructure.messaging.external.sns.core.common.SnsProps;
import org.atlas.infrastructure.messaging.external.sns.core.consumer.BaseSnsMessageConsumer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;

@Component
@Slf4j
public class SnsMessageConsumer extends BaseSnsMessageConsumer implements InitializingBean {

  public SnsMessageConsumer(DomainEventDispatcher domainEventDispatcher,
      SnsProps snsProps,
      SqsClient sqsClient) {
    super(domainEventDispatcher, snsProps, sqsClient);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    consumeMessages("order-event",
        snsProps.getSqsQueueUrl().get(SnsConstant.QUEUE_PRODUCT_SVC_ORDER_EVENT));
  }
}

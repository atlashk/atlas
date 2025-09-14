package org.atlas.infrastructure.messaging.sns.impl.order;

import lombok.extern.slf4j.Slf4j;
import org.atlas.infrastructure.application.context.ApplicationContextService;
import org.atlas.infrastructure.messaging.sns.core.common.SnsConstant;
import org.atlas.infrastructure.messaging.sns.core.common.SnsProps;
import org.atlas.infrastructure.messaging.sns.core.consumer.BaseSnsMessageConsumer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;

@Component
@Slf4j
public class SnsMessageConsumer extends BaseSnsMessageConsumer implements InitializingBean {

  public SnsMessageConsumer(ApplicationContextService applicationContextService,
      SnsProps snsProps,
      SqsClient sqsClient) {
    super(applicationContextService, snsProps, sqsClient);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    consumeMessages("order-event",
        snsProps.getSqsQueueUrl().get(SnsConstant.QUEUE_ORDER_SVC_ORDER_EVENT));
  }
}

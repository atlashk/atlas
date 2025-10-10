package org.atlas.infrastructure.messaging.sns.impl.product.publisher;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.infrastructure.messaging.ProductEventMessagePublisher;
import org.atlas.framework.domain.event.contract.product.BaseProductEvent;
import org.atlas.framework.messaging.publisher.MessagePublisher;
import org.atlas.framework.messaging.publisher.PublishRequest;
import org.atlas.infrastructure.messaging.sns.core.common.SnsProps;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SnsProductEventMessagePublisher implements ProductEventMessagePublisher {

  private final SnsProps snsProps;
  private final MessagePublisher messagePublisher;

  @Override
  public void publish(BaseProductEvent event) {
    PublishRequest request = PublishRequest.builder()
        .destination(snsProps.getSnsTopicArn().get("product_events"))
        .messagePayload(event)
        .build();
    messagePublisher.publish(request);
  }
}

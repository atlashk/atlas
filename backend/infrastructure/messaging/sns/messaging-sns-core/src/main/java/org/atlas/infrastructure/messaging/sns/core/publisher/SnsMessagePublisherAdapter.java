package org.atlas.infrastructure.messaging.sns.core.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.domain.event.contract.order.BaseOrderEvent;
import org.atlas.framework.domain.event.contract.product.BaseProductEvent;
import org.atlas.framework.domain.event.contract.user.BaseUserEvent;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.messaging.publisher.MessagePublisherPort;
import org.atlas.infrastructure.messaging.sns.core.common.SnsConstant;
import org.atlas.infrastructure.messaging.sns.core.common.SnsProps;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class SnsMessagePublisherAdapter implements MessagePublisherPort {

  private final SnsClient snsClient;
  private final SnsProps snsProps;

  @Override
  public void publish(BaseOrderEvent event) {
    this.doPublish(
        event,
        String.valueOf(event.getOrder().getId()),
        snsProps.getSnsTopicArn().get(SnsConstant.SNS_TOPIC_ARN_ORDER_EVENT)
    );
  }

  @Override
  public void publish(BaseProductEvent event) {
    this.doPublish(
        event,
        String.valueOf(event.getProduct().getId()),
        snsProps.getSnsTopicArn().get(SnsConstant.SNS_TOPIC_ARN_PRODUCT_EVENT)
    );
  }

  @Override
  public void publish(BaseUserEvent event) {
    this.doPublish(
        event,
        String.valueOf(event.getUser().getId()),
        snsProps.getSnsTopicArn().get(SnsConstant.SNS_TOPIC_ARN_USER_EVENT)
    );
  }

  @Override
  public void doPublish(Object messagePayload, String messageKey, String snsTopicArn) {
    String message = JsonUtil.getInstance()
        .toJson(messagePayload);
    PublishRequest request = PublishRequest.builder()
        .message(message)
        .topicArn(snsTopicArn)
        .build();
    PublishResponse response = snsClient.publish(request);
    log.info("Published message: payload={}, key={}\nStatus: {}. ApiResponseWrapper: {}",
        messagePayload, messageKey, response.sdkHttpResponse().statusCode(), response);
  }
}

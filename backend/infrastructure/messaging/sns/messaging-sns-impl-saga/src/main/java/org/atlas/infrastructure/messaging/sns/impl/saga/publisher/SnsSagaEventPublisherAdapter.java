package org.atlas.infrastructure.messaging.sns.impl.saga.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.saga.event.SagaEvent;
import org.atlas.framework.saga.event.SagaEventPublisherPort;
import org.atlas.infrastructure.messaging.sns.core.common.SnsProps;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class SnsSagaEventPublisherAdapter implements SagaEventPublisherPort {

  private final SnsClient snsClient;
  private final SnsProps snsProps;

  @Override
  public void publish(SagaEvent event) {
    String message = JsonUtil.getInstance()
        .toJson(event);
    PublishRequest request = PublishRequest.builder()
        .message(message)
        .topicArn(snsTopicArn)
        .build();
    PublishResponse response = snsClient.publish(request);
    log.info("Published message: payload={}, key={}\nStatus: {}. ApiResponseWrapper: {}",
        messagePayload, messageKey, response.sdkHttpResponse().statusCode(), response);
  }
}

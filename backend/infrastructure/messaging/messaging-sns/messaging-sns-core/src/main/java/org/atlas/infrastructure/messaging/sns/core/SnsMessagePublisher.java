package org.atlas.infrastructure.messaging.sns.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.messaging.MessagePublisher;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class SnsMessagePublisher implements MessagePublisher {

  private final SnsClient snsClient;

  @Override
  public void publish(Object messagePayload, String messageKey, String snsTopicArn) {
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

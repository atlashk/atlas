package org.atlas.infrastructure.messaging.sns.core.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.messaging.publisher.MessagePublisherPort;
import org.atlas.framework.messaging.publisher.PublishRequest;
import org.atlas.framework.util.StringUtil;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class SnsMessagePublisherAdapter implements MessagePublisherPort {

  private final SnsClient snsClient;

  @Override
  public void publish(PublishRequest request) {
    final String topicArn = request.getDestination();
    if (StringUtil.isBlank(topicArn)) {
      throw new IllegalArgumentException("Topic ARN must be specified");
    }

    final String messageDeduplicationId =
        StringUtil.nvl(request.getRoutingAttributes().get("messageDeduplicationId"));

    final String messageGroupId =
        StringUtil.nvl(request.getRoutingAttributes().get("messageGroupId"));

    String message = JsonUtil.getInstance().toJson(request.getMessagePayload());
    software.amazon.awssdk.services.sns.model.PublishRequest snsPublishRequest =
        software.amazon.awssdk.services.sns.model.PublishRequest.builder()
            .topicArn(topicArn)
            .message(message)
            .messageDeduplicationId(messageDeduplicationId)
            .messageGroupId(messageGroupId)
            .build();
    PublishResponse snsPublishResponse = snsClient.publish(snsPublishRequest);
    log.info("Published message: payload={}\nStatus: {}. ApiResponseWrapper: {}",
        request.getMessagePayload(),
        snsPublishResponse.sdkHttpResponse().statusCode(),
        snsPublishResponse);
  }
}

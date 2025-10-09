package org.atlas.framework.messaging.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.messaging.publisher.PublishRequest;
import org.springframework.stereotype.Component;

@Component
@Aspect
@RequiredArgsConstructor
@Slf4j
public class OutboxAspect {

  private final OutboxMessageRepository outboxMessageRepository;

  /**
   * Intercept all MessagePublisherPort.publish() calls. Instead of calling the actual publish
   * method, save to outbox first.
   */
  @Around("execution(* org.atlas.framework.messaging.publisher.MessagePublisher.publish(..))")
  public Object aroundPublishMessage(ProceedingJoinPoint joinPoint) throws Throwable {
    Object[] args = joinPoint.getArgs();
    PublishRequest request = (PublishRequest) args[0];

    OutboxMessageEntity outboxMessage = OutboxMessageEntity.builder()
        .publishRequest(JsonUtil.getInstance().toJson(request))
        .status(OutboxMessageStatus.PENDING)
        .retries(0)
        .build();
    outboxMessageRepository.insert(outboxMessage);
    log.info("Inserted outbox message: {}", outboxMessage);

    // Return null or some success indicator instead of proceeding with actual publish
    return null;
  }
}

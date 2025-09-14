package org.atlas.infrastructure.outbox.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.atlas.framework.json.JsonUtil;
import org.springframework.stereotype.Component;

@Component
@Aspect
@RequiredArgsConstructor
@Slf4j
public class OutboxAspect {

  private final OutboxMessageRepository outboxMessageRepository;

  // Intercept all MessagePublisher.publish() calls
  @Around("execution(* org.atlas.framework.messaging.MessagePublisherPort.doPublish(..))")
  public Object aroundPublishMessage(ProceedingJoinPoint joinPoint) throws Throwable {
    Object[] args = joinPoint.getArgs();
    Object messagePayload = args[0];
    String messageKey = (String) args[1];
    String destination = (String) args[2];

    // Instead of calling the actual publish method, save to outbox
    OutboxMessageEntity outboxMessage = newOutboxMessage(messagePayload, messageKey, destination);
    outboxMessageRepository.insert(outboxMessage);
    log.info("Inserted outbox message: {}", outboxMessage);

    // Return null or some success indicator instead of proceeding with actual publish
    return null;
  }

  private OutboxMessageEntity newOutboxMessage(Object messagePayload, String messageKey,
      String destination) {
    OutboxMessageEntity outboxMessage = new OutboxMessageEntity();
    outboxMessage.setMessagePayload(JsonUtil.getInstance().toJson(messagePayload));
    outboxMessage.setMessageClass(messagePayload.getClass().getName());
    outboxMessage.setMessageKey(messageKey);
    outboxMessage.setDestination(destination);
    outboxMessage.setStatus(OutboxMessageStatus.PENDING);
    outboxMessage.setRetries(0);
    return outboxMessage;
  }
}

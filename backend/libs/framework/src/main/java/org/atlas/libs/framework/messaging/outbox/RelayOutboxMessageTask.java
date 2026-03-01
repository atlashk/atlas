package org.atlas.libs.framework.messaging.outbox;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(OutboxMessageRepository.class)
@RequiredArgsConstructor
@Slf4j
public class RelayOutboxMessageTask {

  private static final ExecutorService executor = Executors.newCachedThreadPool();

  private final OutboxMessageRepository outboxMessageRepository;
  private final OutboxMessageService outboxMessageService;

  public void execute() {
    // Find pending outbox messages
    List<OutboxMessageEntity> outboxMessages = outboxMessageRepository.findByStatusOrderByCreatedAt(
        OutboxMessageStatus.PENDING);
    if (outboxMessages.isEmpty()) {
      return;
    }

    // Process each outbox message in parallel
    List<CompletableFuture<Void>> futures = outboxMessages.stream()
        .map(outboxMessage ->
            CompletableFuture.runAsync(() -> {
              outboxMessageService.processOutboxMessage(outboxMessage);
            }, executor)
        )
        .toList();

    // Wait for all parallel tasks to complete
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
  }
}

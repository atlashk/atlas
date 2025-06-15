package org.atlas.infrastructure.messaging.sns.core;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.util.ConcurrentUtil;
import org.springframework.beans.factory.DisposableBean;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@RequiredArgsConstructor
@Slf4j
public abstract class SnsMessageConsumer implements DisposableBean {

  protected final SnsProps snsProps;
  protected final SqsClient sqsClient;

  private final AtomicBoolean isRunning = new AtomicBoolean(true);
  private ExecutorService executorService;

  @Override
  public void destroy() {
    log.info("Shutting down SQS consumer");
    isRunning.set(false);
    ConcurrentUtil.shutdown(executorService);
  }

  protected void consumeMessages(String queueName, String queueUrl) {
    executorService = Executors.newSingleThreadExecutor(r -> {
      Thread thread = new Thread(r, queueName);
      thread.setDaemon(true);
      return thread;
    });

    CompletableFuture.runAsync(() -> {
          log.info("Starting SQS consumer for queue: {}", queueUrl);

          ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
              .queueUrl(queueUrl)
              .maxNumberOfMessages(snsProps.getBatchSize())
              .waitTimeSeconds(snsProps.getWaitTimeSeconds())
              .visibilityTimeout(snsProps.getVisibilityTimeout())
              .build();

          while (isRunning.get()) {
            try {
              List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();
              if (CollectionUtils.isNotEmpty(messages)) {
                messages.forEach(message -> {
                  log.debug("Handling message: {}", message.messageId());
                  try {
                    // Note: Need to enable raw message delivery when create subscription to be able to get message body directly
                    Object messagePayload = JsonUtil.getInstance()
                        .toObject(message.body(), Object.class);
                    handleMessage(messagePayload);
                    deleteMessage(message, queueUrl);
                  } catch (Exception e) {
                    log.error("Failed to handle message: messageId={}", message.messageId(), e);
                  }
                });
              }
            } catch (Exception e) {
              log.error("Failed to receive messages from SQS queue", e);
            }
          }
        }, executorService)
        .exceptionally(throwable -> {
          log.error("Fatal error in SQS consumer", throwable);
          return null;
        });
  }

  protected abstract void handleMessage(Object messagePayload);

  private void deleteMessage(Message message, String queueUrl) {
    DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
        .queueUrl(queueUrl)
        .receiptHandle(message.receiptHandle())
        .build();
    sqsClient.deleteMessage(deleteMessageRequest);
    log.info("Deleted message: messageId={}", message.messageId());
  }
}

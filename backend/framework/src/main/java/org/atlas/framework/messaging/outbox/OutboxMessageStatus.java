package org.atlas.framework.messaging.outbox;

public enum OutboxMessageStatus {

  PENDING,
  PROCESSED,
  FAILED,
}

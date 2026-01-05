package org.atlas.common.framework.messaging.outbox;

public enum OutboxMessageStatus {

  PENDING,
  PROCESSED,
  FAILED,
}

package org.atlas.framework.messaging.gateway.outbox;

public enum OutboxMessageStatus {

  PENDING,
  PROCESSED,
  FAILED,
}

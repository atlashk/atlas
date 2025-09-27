package org.atlas.infrastructure.messaging.outbox.core;

public enum OutboxMessageStatus {

  PENDING,
  PROCESSED,
  FAILED,
}

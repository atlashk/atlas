package org.atlas.infrastructure.messaging.external.outbox.core;

public enum OutboxMessageStatus {

  PENDING,
  PROCESSED,
  FAILED,
}

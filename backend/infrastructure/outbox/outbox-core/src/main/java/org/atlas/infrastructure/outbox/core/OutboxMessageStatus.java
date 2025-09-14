package org.atlas.infrastructure.outbox.core;

public enum OutboxMessageStatus {

  PENDING,
  PROCESSED,
  FAILED,
}

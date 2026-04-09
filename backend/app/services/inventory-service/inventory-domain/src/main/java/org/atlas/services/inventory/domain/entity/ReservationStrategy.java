package org.atlas.services.inventory.domain.entity;

public enum ReservationStrategy {

  CONSTRAINT,
  OPTIMISTIC_LOCK,
  PESSIMISTIC_LOCK,
  DISTRIBUTED_LOCK,
}

package org.atlas.framework.saga.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.entity.DomainEntity;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class SagaCompensationEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private Long compensationId;

  private Long sagaId;

  private Long stepId;

  private SagaCompensationStatus compensationStatus;
}

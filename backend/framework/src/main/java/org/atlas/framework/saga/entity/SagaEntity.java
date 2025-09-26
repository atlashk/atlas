package org.atlas.framework.saga.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.entity.DomainEntity;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class SagaEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer sagaId;

  private Integer sagaName;

  private SagaStatus sagaStatus;
}

package org.atlas.framework.saga.entity;

import java.util.Date;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.framework.domain.entity.DomainEntity;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class SagaEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private Long id;
  private String name;
  private String context;
  private SagaStatus status;
  private Date completedAt;
  private String errorMessage;
}

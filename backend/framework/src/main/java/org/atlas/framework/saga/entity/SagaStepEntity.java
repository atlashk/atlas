package org.atlas.framework.saga.entity;

import java.util.Date;
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
public class SagaStepEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private Long stepId;
  private Long sagaId;
  private String stepName;
  private Integer stepOrder;
  private String applicationName;
  private SagaStepStatus stepStatus;
  private Date completedAt;
  private String errorMessage;
  private String compensationErrorMessage;
}

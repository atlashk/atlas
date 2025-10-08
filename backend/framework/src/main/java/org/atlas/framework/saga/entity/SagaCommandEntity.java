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
public class SagaCommandEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private Long id;
  private Long sagaId;
  private String name;
  private String targetServiceName;
  private SagaCommandStatus status;
  private Date completedAt;
  private String errorMessage;
  private String compensationErrorMessage;
}

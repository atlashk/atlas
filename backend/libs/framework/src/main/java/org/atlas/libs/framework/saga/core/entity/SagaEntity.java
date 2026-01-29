package org.atlas.libs.framework.saga.core.entity;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.common.entity.DomainEntity;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class SagaEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer id;
  private String name;
  private String context;
  private SagaStatus status;
  private Date completedAt;
  private String errorMessage;
}

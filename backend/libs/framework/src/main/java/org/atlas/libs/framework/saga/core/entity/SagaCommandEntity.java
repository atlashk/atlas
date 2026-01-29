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
public class SagaCommandEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer id;
  private Integer sagaId;
  private String name;
  private String targetServiceName;
  private SagaCommandStatus status;
  private Date completedAt;
  private String error;
  private String compensationError;
}

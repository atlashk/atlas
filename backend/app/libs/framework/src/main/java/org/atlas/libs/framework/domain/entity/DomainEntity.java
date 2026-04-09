package org.atlas.libs.framework.domain.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DomainEntity implements Serializable {

  private LocalDateTime createdAt;
  private Integer createdBy;
  private LocalDateTime updatedAt;
  private Integer updatedBy;
  private boolean deleted;
  private Integer version;
}

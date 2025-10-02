package org.atlas.framework.domain.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DomainEntity implements Serializable {

  private Date createdAt;
  private Integer createdBy;
  private Date updatedAt;
  private Integer updatedBy;
  private Boolean deleted = false;
  private Long version;
}

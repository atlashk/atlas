package org.atlas.domain.auth.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.entity.DomainEntity;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class SessionEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private String id;

  private Integer userId;

  private String deviceId;

  private String refreshToken;
}

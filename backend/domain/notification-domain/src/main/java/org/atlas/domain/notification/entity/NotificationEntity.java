package org.atlas.domain.notification.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.entity.DomainEntity;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class NotificationEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer id;
}

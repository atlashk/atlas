package org.atlas.infrastructure.persistence.jpa.adapter.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.atlas.infrastructure.persistence.jpa.core.entity.JpaBaseEntity;

@Entity
@Table(name = "session")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class JpaSessionEntity extends JpaBaseEntity {

  @Id
  @EqualsAndHashCode.Include
  private String id;

  @Column(name = "user_id")
  private Integer userId;

  @Column(name = "device_id")
  private String deviceId;

  @Column(name = "refresh_token")
  private String refreshToken;
}

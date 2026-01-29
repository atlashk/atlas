package org.atlas.libs.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class JpaBaseEntity {

  /**
   * The creation timestamp of the entity. Managed by Spring Data JPA auditing.
   */
  @Column(name = "created_at", nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  @CreatedDate
  protected Date createdAt;

  /**
   * The last update timestamp of the entity. Managed by Spring Data JPA auditing.
   */
  @Column(name = "updated_at")
  @Temporal(TemporalType.TIMESTAMP)
  @LastModifiedDate
  protected Date updatedAt;
}

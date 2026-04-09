package org.atlas.libs.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
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
  @CreatedDate
  protected LocalDateTime createdAt;

  /**
   * The last update timestamp of the entity. Managed by Spring Data JPA auditing.
   */
  @Column(name = "updated_at")
  @LastModifiedDate
  protected LocalDateTime updatedAt;
}

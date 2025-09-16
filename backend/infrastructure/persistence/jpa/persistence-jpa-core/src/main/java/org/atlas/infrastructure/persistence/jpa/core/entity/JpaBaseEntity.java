package org.atlas.infrastructure.persistence.jpa.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public class JpaBaseEntity {

  /**
   * The creation timestamp of the entity.
   * <ul>
   *   <li>`insertable = false`: The value for this column is automatically set by the database during insert operations.</li>
   *   <li>`updatable = false`: The value cannot be updated once set, ensuring it remains as the initial creation time.</li>
   * </ul>
   * Note: This column is intended for auditing purposes, so it cannot be null. The database should ensure its value is generated, e.g., via a default value or trigger.
   */
  @Column(name = "created_at", insertable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  protected Date createdAt;

  /**
   * The last update timestamp of the entity.
   * <ul>
   *   <li>`updatable = false`: The value for this column is automatically set by the database and cannot be manually updated.</li>
   * </ul>
   * Note: This column is intended for auditing purposes, so it cannot be null. Ensure the database populates this value appropriately.
   */
  @Column(name = "updated_at", updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  protected Date updatedAt;
}

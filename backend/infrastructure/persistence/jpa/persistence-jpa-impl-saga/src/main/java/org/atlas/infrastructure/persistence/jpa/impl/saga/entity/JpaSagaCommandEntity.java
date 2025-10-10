package org.atlas.infrastructure.persistence.jpa.impl.saga.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.framework.saga.entity.SagaCommandStatus;
import org.atlas.infrastructure.persistence.jpa.core.entity.JpaBaseEntity;

@Entity
@Table(name = "saga_command")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class JpaSagaCommandEntity extends JpaBaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "saga_id")
  private Integer sagaId;

  @Column(name = "name")
  private String name;

  @Column(name = "target_service_name")
  private String targetServiceName;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private SagaCommandStatus status;

  @Column(name = "completed_at")
  private Date completedAt;

  @Column(name = "compensation_error_message")
  private String compensationErrorMessage;
}

package org.atlas.platform.authorization.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.security.FederatedIdentityProvider;
import org.atlas.libs.persistence.jpa.entity.JpaBaseEntity;

@Entity
@Table(name = "federated_identity")
@IdClass(JpaFederatedIdentityId.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class JpaFederatedIdentityEntity extends JpaBaseEntity {

  @Id
  @Column(name = "user_id")
  @EqualsAndHashCode.Include
  private String userId;

  @Id
  @Column(name = "provider")
  @Enumerated(EnumType.STRING)
  @EqualsAndHashCode.Include
  private FederatedIdentityProvider provider;

  @Column(name = "provider_user_id")
  private String providerUserId;
}

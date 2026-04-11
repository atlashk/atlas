package org.atlas.services.user.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.entity.DomainEntity;
import org.atlas.libs.framework.security.FederatedIdentityProvider;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class FederatedIdentity extends DomainEntity {

  @EqualsAndHashCode.Include
  private String userId;

  @EqualsAndHashCode.Include
  private FederatedIdentityProvider provider;

  private String providerUserId;
}

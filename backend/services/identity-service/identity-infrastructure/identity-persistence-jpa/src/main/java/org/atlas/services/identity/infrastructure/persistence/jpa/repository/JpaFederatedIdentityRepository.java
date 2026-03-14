package org.atlas.services.identity.infrastructure.persistence.jpa.repository;

import java.util.Optional;
import org.atlas.libs.framework.security.FederatedIdentityProvider;
import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.identity.infrastructure.persistence.jpa.entity.JpaFederatedIdentityEntity;
import org.atlas.services.identity.infrastructure.persistence.jpa.entity.JpaFederatedIdentityId;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaFederatedIdentityRepository
    extends JpaBaseRepository<JpaFederatedIdentityEntity, JpaFederatedIdentityId> {

  Optional<JpaFederatedIdentityEntity> findByUserIdAndProvider(
      String userId, FederatedIdentityProvider provider);

  Optional<JpaFederatedIdentityEntity> findByProviderAndProviderUserId(
      FederatedIdentityProvider provider, String providerUserId);
}

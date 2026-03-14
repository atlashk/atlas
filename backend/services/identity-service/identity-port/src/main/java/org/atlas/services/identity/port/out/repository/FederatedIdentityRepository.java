package org.atlas.services.identity.port.out.repository;

import java.util.Optional;
import org.atlas.libs.framework.security.FederatedIdentityProvider;
import org.atlas.services.identity.domain.entity.FederatedIdentityEntity;

public interface FederatedIdentityRepository {

  Optional<FederatedIdentityEntity> findByUserIdAndProvider(
      String userId, FederatedIdentityProvider provider);

  Optional<FederatedIdentityEntity> findByProviderAndProviderUserId(
      FederatedIdentityProvider provider, String providerUserId);

  void insert(FederatedIdentityEntity federatedIdentity);
}

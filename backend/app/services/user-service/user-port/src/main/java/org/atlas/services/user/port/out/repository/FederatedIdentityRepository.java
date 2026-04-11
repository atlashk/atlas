package org.atlas.services.user.port.out.repository;

import java.util.Optional;
import org.atlas.libs.framework.security.FederatedIdentityProvider;
import org.atlas.services.user.domain.entity.FederatedIdentity;

public interface FederatedIdentityRepository {

  Optional<FederatedIdentity> findByUserIdAndProvider(
      String userId, FederatedIdentityProvider provider);

  Optional<FederatedIdentity> findByProviderAndProviderUserId(
      FederatedIdentityProvider provider, String providerUserId);

  void insert(FederatedIdentity federatedIdentity);
}

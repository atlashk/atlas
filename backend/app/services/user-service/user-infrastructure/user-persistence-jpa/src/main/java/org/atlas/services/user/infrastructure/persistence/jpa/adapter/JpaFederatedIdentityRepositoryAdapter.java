package org.atlas.services.user.infrastructure.persistence.jpa.adapter;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.security.FederatedIdentityProvider;
import org.atlas.services.user.domain.entity.FederatedIdentity;
import org.atlas.services.user.infrastructure.persistence.jpa.mapper.JpaFederatedIdentityMapper;
import org.atlas.services.user.infrastructure.persistence.jpa.repository.JpaFederatedIdentityRepository;
import org.atlas.services.user.port.out.repository.FederatedIdentityRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaFederatedIdentityRepositoryAdapter implements FederatedIdentityRepository {

  private final JpaFederatedIdentityRepository jpaFederatedIdentityRepository;

  @Override
  public Optional<FederatedIdentity> findByUserIdAndProvider(
      String userId, FederatedIdentityProvider provider) {
    return jpaFederatedIdentityRepository.findByUserIdAndProvider(userId, provider)
        .map(JpaFederatedIdentityMapper.INSTANCE::toFederatedIdentity);
  }

  @Override
  public Optional<FederatedIdentity> findByProviderAndProviderUserId(
      FederatedIdentityProvider provider, String providerUserId) {
    return jpaFederatedIdentityRepository.findByProviderAndProviderUserId(provider, providerUserId)
        .map(JpaFederatedIdentityMapper.INSTANCE::toFederatedIdentity);
  }

  @Override
  public void insert(FederatedIdentity federatedIdentity) {
    jpaFederatedIdentityRepository.insert(
        JpaFederatedIdentityMapper.INSTANCE.toJpaFederatedIdentity(federatedIdentity));
  }
}

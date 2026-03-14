package org.atlas.services.identity.infrastructure.persistence.jpa.adapter;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.security.FederatedIdentityProvider;
import org.atlas.services.identity.domain.entity.FederatedIdentityEntity;
import org.atlas.services.identity.infrastructure.persistence.jpa.mapper.JpaFederatedIdentityMapper;
import org.atlas.services.identity.infrastructure.persistence.jpa.repository.JpaFederatedIdentityRepository;
import org.atlas.services.identity.port.out.repository.FederatedIdentityRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaFederatedIdentityRepositoryAdapter implements FederatedIdentityRepository {

  private final JpaFederatedIdentityRepository jpaFederatedIdentityRepository;

  @Override
  public Optional<FederatedIdentityEntity> findByUserIdAndProvider(
      String userId, FederatedIdentityProvider provider) {
    return jpaFederatedIdentityRepository.findByUserIdAndProvider(userId, provider)
        .map(JpaFederatedIdentityMapper.INSTANCE::toFederatedIdentity);
  }

  @Override
  public Optional<FederatedIdentityEntity> findByProviderAndProviderUserId(
      FederatedIdentityProvider provider, String providerUserId) {
    return jpaFederatedIdentityRepository.findByProviderAndProviderUserId(provider, providerUserId)
        .map(JpaFederatedIdentityMapper.INSTANCE::toFederatedIdentity);
  }

  @Override
  public void insert(FederatedIdentityEntity federatedIdentity) {
    jpaFederatedIdentityRepository.insert(
        JpaFederatedIdentityMapper.INSTANCE.toJpaFederatedIdentity(federatedIdentity));
  }
}

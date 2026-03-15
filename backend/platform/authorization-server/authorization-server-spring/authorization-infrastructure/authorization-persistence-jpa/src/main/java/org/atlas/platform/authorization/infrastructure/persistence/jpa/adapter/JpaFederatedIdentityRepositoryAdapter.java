package org.atlas.platform.authorization.infrastructure.persistence.jpa.adapter;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.security.FederatedIdentityProvider;
import org.atlas.platform.authorization.domain.entity.FederatedIdentityEntity;
import org.atlas.platform.authorization.infrastructure.persistence.jpa.mapper.JpaFederatedIdentityMapper;
import org.atlas.platform.authorization.infrastructure.persistence.jpa.repository.JpaFederatedIdentityRepository;
import org.atlas.platform.authorization.port.out.repository.FederatedIdentityRepository;
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

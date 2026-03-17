package org.atlas.platform.authorization.spring.application.core.sso;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.domain.shared.identity.UserRole;
import org.atlas.libs.framework.security.FederatedIdentityProvider;
import org.atlas.libs.framework.security.JwtUtil;
import org.atlas.libs.framework.security.Principal;
import org.atlas.libs.framework.sequencegenerator.SequenceGenerator;
import org.atlas.libs.framework.sequencegenerator.SequenceType;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.platform.authorization.domain.entity.FederatedIdentityEntity;
import org.atlas.platform.authorization.domain.entity.UserEntity;
import org.atlas.platform.authorization.domain.error.DomainError;
import org.atlas.platform.authorization.domain.exception.DomainException;
import org.atlas.platform.authorization.port.in.authentication.model.LoginOutput;
import org.atlas.platform.authorization.port.out.repository.FederatedIdentityRepository;
import org.atlas.platform.authorization.port.out.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SsoLoginPostService {

  private final UserRepository userRepository;
  private final FederatedIdentityRepository federatedIdentityRepository;
  private final SequenceGenerator sequenceGenerator;

  @Transactional
  public LoginOutput loginWithFederatedIdentity(FederatedIdentityProvider provider,
      OAuth2UserInfo oAuth2UserInfo) throws Exception {
    UserEntity user = resolveUser(provider, oAuth2UserInfo);
    Principal principal = user.toPrincipal();
    return new LoginOutput(
        JwtUtil.issueAccessToken(principal),
        JwtUtil.issueRefreshToken(principal)
    );
  }

  private UserEntity resolveUser(FederatedIdentityProvider provider, OAuth2UserInfo oAuth2UserInfo) {
    Optional<FederatedIdentityEntity> federatedIdentity = federatedIdentityRepository
        .findByProviderAndProviderUserId(provider, oAuth2UserInfo.getProviderUserId());
    if (federatedIdentity.isPresent()) {
      return userRepository.findById(federatedIdentity.get().getUserId())
          .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));
    }

    UserEntity user = userRepository.findByEmail(oAuth2UserInfo.getEmail())
        .orElseGet(() -> createUserForFederatedIdentity(oAuth2UserInfo));

    if (federatedIdentityRepository.findByUserIdAndProvider(user.getId(), provider).isEmpty()) {
      FederatedIdentityEntity newFederatedIdentity = FederatedIdentityEntity.builder()
          .userId(user.getId())
          .provider(provider)
          .providerUserId(oAuth2UserInfo.getProviderUserId())
          .build();
      federatedIdentityRepository.insert(newFederatedIdentity);
    }

    return user;
  }

  private UserEntity createUserForFederatedIdentity(OAuth2UserInfo oAuth2UserInfo) {
    String userId = sequenceGenerator.generate(SequenceType.USER);
    UserEntity user = UserEntity.builder()
        .id(userId)
        .password(null)
        .firstName(StringUtil.defaultIfBlank(oAuth2UserInfo.getFirstName(), "Federated"))
        .lastName(StringUtil.defaultIfBlank(oAuth2UserInfo.getLastName(), "User"))
        .email(oAuth2UserInfo.getEmail())
        .role(UserRole.USER)
        .build();
    userRepository.insert(user);
    return user;
  }
}

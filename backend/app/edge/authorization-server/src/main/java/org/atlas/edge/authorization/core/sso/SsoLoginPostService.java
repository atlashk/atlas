package org.atlas.edge.authorization.core.sso;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.domain.exception.DomainException;
import org.atlas.libs.framework.domain.shared.user.UserRole;
import org.atlas.libs.framework.security.FederatedIdentityProvider;
import org.atlas.libs.framework.security.jwt.JwtUtil;
import org.atlas.libs.framework.security.Principal;
import org.atlas.libs.framework.sequencegenerator.SequenceGenerator;
import org.atlas.libs.framework.sequencegenerator.SequenceType;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.edge.authorization.api.model.LoginResponse;
import org.atlas.services.user.port.out.repository.FederatedIdentityRepository;
import org.atlas.services.user.port.out.repository.UserRepository;
import org.atlas.services.user.domain.entity.FederatedIdentity;
import org.atlas.services.user.domain.entity.User;
import org.atlas.services.user.domain.error.UserDomainError;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SsoLoginPostService {

  private final UserRepository userRepository;
  private final FederatedIdentityRepository federatedIdentityRepository;
  private final SequenceGenerator sequenceGenerator;

  @Transactional
  public LoginResponse loginWithFederatedIdentity(FederatedIdentityProvider provider,
      OAuth2UserInfo oAuth2UserInfo) throws Exception {
    User user = resolveUser(provider, oAuth2UserInfo);
    Principal principal = user.toPrincipal();
    return new LoginResponse(
        JwtUtil.issueAccessToken(principal),
        JwtUtil.issueRefreshToken(principal)
    );
  }

  private User resolveUser(FederatedIdentityProvider provider, OAuth2UserInfo oAuth2UserInfo) {
    Optional<FederatedIdentity> federatedIdentity = federatedIdentityRepository
        .findByProviderAndProviderUserId(provider, oAuth2UserInfo.getProviderUserId());
    if (federatedIdentity.isPresent()) {
      return userRepository.findById(federatedIdentity.get().getUserId())
          .orElseThrow(() -> new DomainException(UserDomainError.USER_NOT_FOUND));
    }

    User user = userRepository.findByEmail(oAuth2UserInfo.getEmail())
        .orElseGet(() -> createUserForFederatedIdentity(oAuth2UserInfo));

    if (federatedIdentityRepository.findByUserIdAndProvider(user.getId(), provider).isEmpty()) {
      FederatedIdentity newFederatedIdentity = FederatedIdentity.builder()
          .userId(user.getId())
          .provider(provider)
          .providerUserId(oAuth2UserInfo.getProviderUserId())
          .build();
      federatedIdentityRepository.insert(newFederatedIdentity);
    }

    return user;
  }

  private User createUserForFederatedIdentity(OAuth2UserInfo oAuth2UserInfo) {
    String userId = sequenceGenerator.generate(SequenceType.USER);
    User user = User.builder()
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

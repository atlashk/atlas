package org.atlas.services.identity.application.spring.core;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.domain.shared.identity.UserRole;
import org.atlas.libs.framework.security.FederatedIdentityProvider;
import org.atlas.libs.framework.sequencegenerator.SequenceGenerator;
import org.atlas.libs.framework.sequencegenerator.SequenceType;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.libs.jwt.IssueTokenInput;
import org.atlas.libs.jwt.JwtUtil;
import org.atlas.services.identity.domain.entity.FederatedIdentityEntity;
import org.atlas.services.identity.domain.entity.UserEntity;
import org.atlas.services.identity.domain.error.DomainError;
import org.atlas.services.identity.domain.exception.DomainException;
import org.atlas.services.identity.port.in.authentication.model.LoginOutput;
import org.atlas.services.identity.port.out.repository.FederatedIdentityRepository;
import org.atlas.services.identity.port.out.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuth2LoginPostService {

  private final UserRepository userRepository;
  private final FederatedIdentityRepository federatedIdentityRepository;
  private final SequenceGenerator sequenceGenerator;

  @Transactional
  public LoginOutput loginWithFederatedIdentity(FederatedIdentityProvider provider,
      String providerUserId, String email, String firstName, String lastName) throws Exception {
    if (provider == null || StringUtil.isBlank(providerUserId) || StringUtil.isBlank(email)) {
      throw new DomainException(DomainError.USER_REGISTRATION_FAILED);
    }

    UserEntity user = resolveUser(provider, providerUserId, email, firstName, lastName);
    IssueTokenInput issueTokenInput = IssueTokenInput.builder()
        .userId(user.getId())
        .role(user.getRole())
        .build();

    return new LoginOutput(
        JwtUtil.issueAccessToken(issueTokenInput),
        JwtUtil.issueRefreshToken(issueTokenInput)
    );
  }

  private UserEntity resolveUser(FederatedIdentityProvider provider, String providerUserId,
      String email, String firstName, String lastName) {
    Optional<FederatedIdentityEntity> federatedIdentity = federatedIdentityRepository
        .findByProviderAndProviderUserId(provider, providerUserId);
    if (federatedIdentity.isPresent()) {
      return userRepository.findById(federatedIdentity.get().getUserId())
          .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));
    }

    UserEntity user = userRepository.findByEmail(email)
        .orElseGet(() -> createUserForFederatedIdentity(email, firstName, lastName));

    if (federatedIdentityRepository.findByUserIdAndProvider(user.getId(), provider).isEmpty()) {
      FederatedIdentityEntity newFederatedIdentity = FederatedIdentityEntity.builder()
          .userId(user.getId())
          .provider(provider)
          .providerUserId(providerUserId)
          .build();
      federatedIdentityRepository.insert(newFederatedIdentity);
    }

    return user;
  }

  private UserEntity createUserForFederatedIdentity(
      String email,
      String firstName,
      String lastName) {
    String userId = sequenceGenerator.generate(SequenceType.USER);

    UserEntity user = UserEntity.builder()
        .id(userId)
        .password(null)
        .firstName(StringUtil.defaultIfBlank(firstName, "Federated"))
        .lastName(StringUtil.defaultIfBlank(lastName, "User"))
        .email(email)
        .role(UserRole.USER)
        .build();
    userRepository.insert(user);
    return user;
  }
}

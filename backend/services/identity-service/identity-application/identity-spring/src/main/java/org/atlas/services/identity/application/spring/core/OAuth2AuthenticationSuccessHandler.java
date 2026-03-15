package org.atlas.services.identity.application.spring.core;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.security.FederatedIdentityProvider;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.services.identity.port.in.authentication.model.LoginOutput;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  private static final Map<FederatedIdentityProvider, List<String>> PROVIDER_USER_ID_ATTRIBUTES =
      Map.of(
          FederatedIdentityProvider.GOOGLE, List.of("sub", "id")
      );
  private static final List<String> EMAIL_ATTRIBUTES = List.of("email");
  private static final List<String> FIRST_NAME_ATTRIBUTES = List.of("given_name", "first_name",
      "name");
  private static final List<String> LAST_NAME_ATTRIBUTES = List.of("family_name", "last_name");

  private final OAuth2LoginPostService oAuth2LoginPostService;

  @Value("${app.oauth2.frontend.success-url:http://localhost:8000/login}")
  private String successUrl;

  @Value("${app.oauth2.frontend.failure-url:http://localhost:8000/login}")
  private String failureUrl;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    if (!(authentication instanceof OAuth2AuthenticationToken oauth2Token)) {
      response.sendRedirect(failureUrl + "?ssoError=invalid_oauth2_authentication");
      return;
    }

    FederatedIdentityProvider provider = resolveProvider(oauth2Token);
    OAuth2User oauth2User = oauth2Token.getPrincipal();
    String providerUserId = resolveFirstNonBlankAttribute(oauth2User,
        PROVIDER_USER_ID_ATTRIBUTES.getOrDefault(provider, List.of("sub", "id", "user_id")));
    String email = resolveFirstNonBlankAttribute(oauth2User, EMAIL_ATTRIBUTES);
    String firstName = resolveFirstNonBlankAttribute(oauth2User, FIRST_NAME_ATTRIBUTES);
    String lastName = resolveFirstNonBlankAttribute(oauth2User, LAST_NAME_ATTRIBUTES);

    try {
      LoginOutput loginOutput = oAuth2LoginPostService.loginWithFederatedIdentity(
          provider, providerUserId, email, firstName, lastName);
      String redirectUrl = UriComponentsBuilder.fromUriString(successUrl)
          .queryParam("accessToken", loginOutput.getAccessToken())
          .queryParam("refreshToken", loginOutput.getRefreshToken())
          .queryParam("provider", provider.name().toLowerCase(Locale.ROOT))
          .build(true)
          .toUriString();
      response.sendRedirect(redirectUrl);
    } catch (Exception e) {
      String redirectUrl = UriComponentsBuilder.fromUriString(failureUrl)
          .queryParam("ssoError", "federated_login_failed")
          .queryParam("provider", provider.name().toLowerCase(Locale.ROOT))
          .build(true)
          .toUriString();
      response.sendRedirect(redirectUrl);
    }
  }

  private FederatedIdentityProvider resolveProvider(OAuth2AuthenticationToken oauth2Token) {
    String registrationId = oauth2Token.getAuthorizedClientRegistrationId();
    if (StringUtil.isBlank(registrationId)) {
      throw new IllegalArgumentException("OAuth2 registration id is missing");
    }
    return FederatedIdentityProvider.valueOf(registrationId.trim().toUpperCase(Locale.ROOT));
  }

  private String resolveFirstNonBlankAttribute(OAuth2User user, List<String> keys) {
    for (String key : keys) {
      String value = user.getAttribute(key);
      if (value != null && !value.isBlank()) {
        return value;
      }
    }
    return null;
  }
}

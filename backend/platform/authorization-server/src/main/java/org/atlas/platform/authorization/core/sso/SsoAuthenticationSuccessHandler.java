package org.atlas.platform.authorization.core.sso;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.security.FederatedIdentityProvider;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.platform.authorization.api.model.LoginResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class SsoAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  private final SsoLoginPostService ssoLoginPostService;
  private final List<OAuth2UserInfoExtractor> oAuth2UserInfoExtractors;

  @Value("${app.oauth2.callback-url:http://localhost:8000/login/callback}")
  private String callbackUrl;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    if (!(authentication instanceof OAuth2AuthenticationToken oauth2Token)) {
      response.sendRedirect(callbackUrl + "?ssoError=invalid_oauth2_authentication");
      return;
    }

    FederatedIdentityProvider provider = resolveProvider(oauth2Token);

    // Resolve OAuth2 user info
    OAuth2User oauth2User = oauth2Token.getPrincipal();
    OAuth2UserInfoExtractor oAuth2UserInfoExtractor = oAuth2UserInfoExtractors.stream()
        .filter(extractor -> extractor.supports(provider))
        .findFirst()
        .orElseThrow(() ->
            new IllegalArgumentException(
                "No OAuth2 user info extractor for provider: " + provider));
    OAuth2UserInfo oAuth2UserInfo = oAuth2UserInfoExtractor.extract(oauth2User);
    if (oAuth2UserInfo.isInvalid()) {
      throw new IllegalArgumentException("Invalid OAuth2 user info");
    }

    try {
      LoginResponse loginResponse = ssoLoginPostService.loginWithFederatedIdentity(
          provider, oAuth2UserInfo);
      String redirectUrl = UriComponentsBuilder.fromUriString(callbackUrl)
          .queryParam("accessToken", loginResponse.getAccessToken())
          .queryParam("refreshToken", loginResponse.getRefreshToken())
          .queryParam("provider", provider.name().toLowerCase(Locale.ROOT))
          .build(true)
          .toUriString();
      response.sendRedirect(redirectUrl);
    } catch (Exception e) {
      String redirectUrl = UriComponentsBuilder.fromUriString(callbackUrl)
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
}

package org.atlas.platform.authorization.core.sso;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class SsoAuthenticationFailureHandler implements AuthenticationFailureHandler {

  @Value("${app.oauth2.callback-url:http://localhost:8000/login/callback}")
  private String callbackUrl;

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException exception) throws IOException, ServletException {
    String provider = resolveProvider(request);
    String redirectUrl = UriComponentsBuilder.fromUriString(callbackUrl)
        .queryParam("ssoError", "federated_authentication_failed")
        .queryParam("provider", provider)
        .build(true)
        .toUriString();
    response.sendRedirect(redirectUrl);
  }

  private String resolveProvider(HttpServletRequest request) {
    String uri = request.getRequestURI();
    if (uri == null || uri.isBlank()) {
      return "unknown";
    }
    String[] parts = uri.split("/");
    if (parts.length == 0) {
      return "unknown";
    }
    return parts[parts.length - 1].toLowerCase(Locale.ROOT);
  }
}

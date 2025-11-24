package org.atlas.edge.auth.springsecurityjwt.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CookieService {

  public void addCookie(HttpServletResponse response, String cookieName, String cookieValue,
      long cookieExpiryInSeconds) {
    ResponseCookie responseCookie = ResponseCookie.from(cookieName, cookieValue)
        // Prevents JavaScript from accessing the cookie (helps protect against XSS attacks)
        .httpOnly(true)
        // Ensures the cookie is only sent over HTTPS (should always be true in production)
        .secure(true)
        // Allows cookie to be sent in cross-site requests (required for cross-origin setups like frontend and backend on different domains)
        .sameSite("None")
        // Sets the cookie to be accessible for the entire domain (can be restricted further if needed)
        .path("/")
        .maxAge(cookieExpiryInSeconds)
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
  }

  public void deleteCookie(HttpServletResponse response, String cookieName) {
    addCookie(response, cookieName, null, 0);
  }
}

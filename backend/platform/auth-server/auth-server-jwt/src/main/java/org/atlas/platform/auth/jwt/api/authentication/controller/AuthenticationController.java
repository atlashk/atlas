package org.atlas.platform.auth.jwt.api.authentication.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.domain.common.error.DomainError;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.platform.auth.jwt.api.authentication.model.GenerateOneTimeTokenRequest;
import org.atlas.platform.auth.jwt.api.authentication.model.GenerateOneTimeTokenResponse;
import org.atlas.platform.auth.jwt.api.authentication.model.LoginRequest;
import org.atlas.platform.auth.jwt.api.authentication.model.LoginResponse;
import org.atlas.platform.auth.jwt.api.authentication.model.OneTimeTokenLoginRequest;
import org.atlas.platform.auth.jwt.api.authentication.model.RefreshTokenRequest;
import org.atlas.platform.auth.jwt.api.authentication.model.RefreshTokenResponse;
import org.atlas.platform.auth.jwt.api.authentication.service.AuthenticationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/authentication")
@Validated
@RequiredArgsConstructor
public class AuthenticationController {

  private final AuthenticationService authenticationService;
  private final OneTimeTokenService oneTimeTokenService;

  @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "User login")
  public ApiResponseWrapper<LoginResponse> login(
      @Parameter(description = "Request object containing user credentials for login", required = true)
      @Valid @RequestBody LoginRequest request) throws Exception {
    LoginResponse response = authenticationService.login(request);
    return ApiResponseWrapper.success(response);
  }

  @PostMapping("/ott/login")
  @Operation(summary = "One-time token login")
  public ApiResponseWrapper<LoginResponse> oneTimeTokenLogin(
      @Valid @RequestBody OneTimeTokenLoginRequest request) throws Exception {
    LoginResponse response = authenticationService.oneTimeTokenLogin(request);
    return ApiResponseWrapper.success(response);
  }

  @PostMapping("/ott/generate")
  @Operation(summary = "Generate one-time token")
  public ApiResponseWrapper<GenerateOneTimeTokenResponse> generateOneTimeToken(
      @Valid @RequestBody GenerateOneTimeTokenRequest request) {
    OneTimeToken token = oneTimeTokenService.generate(
        new org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest(
            request.getUsername()));
    GenerateOneTimeTokenResponse response = new GenerateOneTimeTokenResponse(token.getTokenValue());
    return ApiResponseWrapper.success(response);
  }

  @PostMapping(value = "/refresh-token", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Refresh token")
  public ApiResponseWrapper<RefreshTokenResponse> refreshToken(
      @Parameter(description = "Refresh token sent in the request body", required = true)
      @Valid @RequestBody RefreshTokenRequest request) throws Exception {
    RefreshTokenResponse response = authenticationService.refreshToken(request);
    return ApiResponseWrapper.success(response);
  }

  @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "User logout")
  public ApiResponseWrapper<Void> logout(HttpServletRequest request) throws Exception {
    String accessToken = null;
    String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authorization != null && authorization.startsWith("Bearer ")) {
      accessToken = authorization.substring("Bearer ".length());
    }
    if (StringUtil.isBlank(accessToken)) {
      return ApiResponseWrapper.error(DomainError.UNAUTHORIZED.getErrorCode(),
          "Missing access token");
    }
    authenticationService.logout(accessToken);
    return ApiResponseWrapper.success();
  }
}

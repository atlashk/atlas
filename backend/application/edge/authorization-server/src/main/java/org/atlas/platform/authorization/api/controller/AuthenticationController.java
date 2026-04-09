package org.atlas.platform.authorization.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.rest.ApiResponseWrapper;
import org.atlas.libs.framework.domain.error.CommonDomainError;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.platform.authorization.api.model.ChangePasswordRequest;
import org.atlas.platform.authorization.api.model.GenerateOneTimeTokenRequest;
import org.atlas.platform.authorization.api.model.GenerateOneTimeTokenResponse;
import org.atlas.platform.authorization.api.model.LoginRequest;
import org.atlas.platform.authorization.api.model.LoginResponse;
import org.atlas.platform.authorization.api.model.OneTimeTokenLoginRequest;
import org.atlas.platform.authorization.api.model.RefreshTokenRequest;
import org.atlas.platform.authorization.api.model.RefreshTokenResponse;
import org.atlas.platform.authorization.api.service.AuthenticationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authentication")
@Validated
@RequiredArgsConstructor
public class AuthenticationController {

  private final AuthenticationService authenticationService;

  @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "User login")
  public ApiResponseWrapper<LoginResponse> login(
      @Parameter(description = "Request object containing user credentials for login", required = true)
      @Valid @RequestBody LoginRequest request) throws Exception {
    LoginResponse responseData = authenticationService.login(request);
    return ApiResponseWrapper.success(responseData);
  }

  @PostMapping(value = "/refresh-token", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Refresh token")
  public ApiResponseWrapper<RefreshTokenResponse> refreshToken(
      @Parameter(description = "Refresh token sent in the request body", required = true)
      @Valid @RequestBody RefreshTokenRequest request) throws Exception {
    RefreshTokenResponse responseData = authenticationService.refreshToken(request);
    return ApiResponseWrapper.success(responseData);
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
      return ApiResponseWrapper.error(CommonDomainError.UNAUTHORIZED.getErrorCode(),
          "Missing access token");
    }
    authenticationService.logout(accessToken);
    return ApiResponseWrapper.success();
  }

  @PostMapping(value = "/change-password", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Change user password")
  public ApiResponseWrapper<Void> changePassword(
      @Parameter(description = "Request object containing the needed information to change user password", required = true)
      @Valid @RequestBody ChangePasswordRequest request) {
    authenticationService.changePassword(request);
    return ApiResponseWrapper.success();
  }

  @PostMapping(value = "/ott/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "One-time token login")
  public ApiResponseWrapper<LoginResponse> oneTimeTokenLogin(
      @Valid @RequestBody OneTimeTokenLoginRequest request) throws Exception {
    LoginResponse responseData = authenticationService.oneTimeTokenLogin(request);
    return ApiResponseWrapper.success(responseData);
  }

  @PostMapping(value = "/ott/generate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Generate one-time token")
  public ApiResponseWrapper<GenerateOneTimeTokenResponse> generateOneTimeToken(
      @Valid @RequestBody GenerateOneTimeTokenRequest request) {
    GenerateOneTimeTokenResponse responseData = authenticationService.generateOneTimeToken(request);
    return ApiResponseWrapper.success(responseData);
  }
}

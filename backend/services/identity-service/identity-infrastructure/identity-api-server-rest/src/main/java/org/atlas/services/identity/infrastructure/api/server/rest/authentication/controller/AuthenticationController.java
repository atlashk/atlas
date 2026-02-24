package org.atlas.services.identity.infrastructure.api.server.rest.authentication.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.domain.error.DomainError;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.services.identity.infrastructure.api.server.rest.authentication.mapper.AuthenticationMapper;
import org.atlas.services.identity.infrastructure.api.server.rest.authentication.model.GenerateOneTimeTokenRequest;
import org.atlas.services.identity.infrastructure.api.server.rest.authentication.model.GenerateOneTimeTokenResponse;
import org.atlas.services.identity.infrastructure.api.server.rest.authentication.model.LoginRequest;
import org.atlas.services.identity.infrastructure.api.server.rest.authentication.model.LoginResponse;
import org.atlas.services.identity.infrastructure.api.server.rest.authentication.model.OneTimeTokenLoginRequest;
import org.atlas.services.identity.infrastructure.api.server.rest.authentication.model.RefreshTokenRequest;
import org.atlas.services.identity.infrastructure.api.server.rest.authentication.model.RefreshTokenResponse;
import org.atlas.services.identity.infrastructure.api.server.rest.user.mapper.UserMapper;
import org.atlas.services.identity.infrastructure.api.server.rest.authentication.model.ChangePasswordRequest;
import org.atlas.services.identity.port.in.authentication.model.GenerateOneTimeTokenInput;
import org.atlas.services.identity.port.in.authentication.model.GenerateOneTimeTokenOutput;
import org.atlas.services.identity.port.in.authentication.model.LoginInput;
import org.atlas.services.identity.port.in.authentication.model.LoginOutput;
import org.atlas.services.identity.port.in.authentication.model.OneTimeTokenLoginInput;
import org.atlas.services.identity.port.in.authentication.model.RefreshTokenInput;
import org.atlas.services.identity.port.in.authentication.model.RefreshTokenOutput;
import org.atlas.services.identity.port.in.authentication.service.AuthenticationService;
import org.atlas.services.identity.port.in.authentication.model.ChangePasswordInput;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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

  @GetMapping(value = "/.well-known/jwks.json")
  @Operation(summary = "JwkSet endpoint")
  public Map<String, Object> jwkSet() throws Exception {
    return authenticationService.jwkSet();
  }

  @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "User login")
  public ApiResponseWrapper<LoginResponse> login(
      @Parameter(description = "Request object containing user credentials for login", required = true)
      @Valid @RequestBody LoginRequest request) throws Exception {
    LoginInput input = AuthenticationMapper.INSTANCE.toLoginInput(request);
    LoginOutput output = authenticationService.login(input);
    LoginResponse responseData = AuthenticationMapper.INSTANCE.toLoginResponse(output);
    return ApiResponseWrapper.success(responseData);
  }

  @PostMapping(value = "/refresh-token", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Refresh token")
  public ApiResponseWrapper<RefreshTokenResponse> refreshToken(
      @Parameter(description = "Refresh token sent in the request body", required = true)
      @Valid @RequestBody RefreshTokenRequest request) throws Exception {
    RefreshTokenInput input = AuthenticationMapper.INSTANCE.toRefreshTokenInput(request);
    RefreshTokenOutput output = authenticationService.refreshToken(input);
    RefreshTokenResponse responseData = AuthenticationMapper.INSTANCE.toRefreshTokenResponse(output);
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
      return ApiResponseWrapper.error(DomainError.UNAUTHORIZED.getErrorCode(),
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
    ChangePasswordInput input = UserMapper.INSTANCE.toChangePasswordInput(request);
    authenticationService.changePassword(input);
    return ApiResponseWrapper.success();
  }

  @PostMapping(value = "/ott/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "One-time token login")
  public ApiResponseWrapper<LoginResponse> oneTimeTokenLogin(
      @Valid @RequestBody OneTimeTokenLoginRequest request) throws Exception {
    OneTimeTokenLoginInput input = AuthenticationMapper.INSTANCE.toOneTimeTokenLoginInput(request);
    LoginOutput output = authenticationService.oneTimeTokenLogin(input);
    LoginResponse responseData = AuthenticationMapper.INSTANCE.toLoginResponse(output);
    return ApiResponseWrapper.success(responseData);
  }

  @PostMapping(value = "/ott/generate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Generate one-time token")
  public ApiResponseWrapper<GenerateOneTimeTokenResponse> generateOneTimeToken(
      @Valid @RequestBody GenerateOneTimeTokenRequest request) {
    GenerateOneTimeTokenInput input = AuthenticationMapper.INSTANCE
        .toGenerateOneTimeTokenInput(request);
    GenerateOneTimeTokenOutput output = authenticationService.generateOneTimeToken(input);
    GenerateOneTimeTokenResponse responseData =
        AuthenticationMapper.INSTANCE.toGenerateOneTimeTokenResponse(output);
    return ApiResponseWrapper.success(responseData);
  }
}

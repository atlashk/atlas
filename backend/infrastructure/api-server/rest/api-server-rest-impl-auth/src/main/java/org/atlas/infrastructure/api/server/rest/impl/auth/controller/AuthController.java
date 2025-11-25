package org.atlas.infrastructure.api.server.rest.impl.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.auth.service.AuthService;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.infrastructure.api.server.rest.impl.auth.model.GenerateOneTimeTokenRequest;
import org.atlas.infrastructure.api.server.rest.impl.auth.model.GenerateOneTimeTokenResponse;
import org.atlas.infrastructure.api.server.rest.impl.auth.model.LoginRequest;
import org.atlas.infrastructure.api.server.rest.impl.auth.model.LoginResponse;
import org.atlas.infrastructure.api.server.rest.impl.auth.model.OneTimeTokenLoginRequest;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
@Validated
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "User login")
  public ApiResponseWrapper<LoginResponse> login(
      @Parameter(description = "Request object containing user credentials for login", required = true)
      @Valid @RequestBody LoginRequest request) throws Exception {
    LoginResponse loginResponse = authService.login(request);
    return ApiResponseWrapper.success(loginResponse);
  }

  @PostMapping("/ott/login")
  @Operation(summary = "One-time token login")
  public ApiResponseWrapper<LoginResponse> oneTimeTokenLogin(
      @Valid @RequestBody OneTimeTokenLoginRequest request) throws Exception {
    LoginResponse loginResponse = authService.oneTimeTokenLogin(request);
    return ApiResponseWrapper.success(loginResponse);
  }

  @PostMapping("/ott/generate")
  @Operation(summary = "Generate one-time token")
  public ApiResponseWrapper<GenerateOneTimeTokenResponse> generateOneTimeToken(
      @Valid @RequestBody GenerateOneTimeTokenRequest request) {
    GenerateOneTimeTokenResponse response = authService.generateOneTimeToken(request);
    return ApiResponseWrapper.success(response);
  }

  @PostMapping(value = "/refresh-token", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Refresh token")
  public ApiResponseWrapper<RefreshTokenResponse> refreshToken(
      @Parameter(description = "Refresh token sent in the request body", required = true)
      @Valid @RequestBody RefreshTokenRequest request) throws Exception {
    RefreshTokenResponse response = authService.refreshToken(request);
    return ApiResponseWrapper.success(response);
  }

  @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "User logout")
  public ApiResponseWrapper<Void> logout() throws Exception {
    authService.logout();
    return ApiResponseWrapper.success();
  }
}

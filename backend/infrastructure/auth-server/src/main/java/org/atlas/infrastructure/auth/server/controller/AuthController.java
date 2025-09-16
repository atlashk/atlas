package org.atlas.infrastructure.auth.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.infrastructure.auth.server.model.GenerateOneTimeTokenRequest;
import org.atlas.infrastructure.auth.server.model.GenerateOneTimeTokenResponse;
import org.atlas.infrastructure.auth.server.model.LoginRequest;
import org.atlas.infrastructure.auth.server.model.LoginResponse;
import org.atlas.infrastructure.auth.server.model.OneTimeTokenLoginRequest;
import org.atlas.infrastructure.auth.server.model.RefreshTokenRequest;
import org.atlas.infrastructure.auth.server.model.RefreshTokenResponse;
import org.atlas.infrastructure.auth.server.service.AuthService;
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

  @Operation(
      summary = "User Login",
      description = "Authenticates a user using username, email, or phone number and returns a login response."
  )
  @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseWrapper<LoginResponse> login(
      @Parameter(description = "Request object containing user credentials for login.", required = true)
      @Valid @RequestBody LoginRequest request) throws Exception {
    LoginResponse loginResponse = authService.login(request);
    return ApiResponseWrapper.success(loginResponse);
  }

  @Operation(
      summary = "One-Time Token Login",
      description = "Logs in a user using a valid one-time token and returns access and refresh tokens."
  )
  @PostMapping("/ott/login")
  public ApiResponseWrapper<LoginResponse> oneTimeTokenLogin(
      @Valid @RequestBody OneTimeTokenLoginRequest request) throws Exception {
    LoginResponse loginResponse = authService.oneTimeTokenLogin(request);
    return ApiResponseWrapper.success(loginResponse);
  }

  @Operation(
      summary = "Generate One-Time Token",
      description = "Generates a new one-time token for temporary login."
  )
  @PostMapping("/ott/generate")
  public ApiResponseWrapper<GenerateOneTimeTokenResponse> generateOneTimeToken(
      @Valid @RequestBody GenerateOneTimeTokenRequest request) {
    GenerateOneTimeTokenResponse response = authService.generateOneTimeToken(request);
    return ApiResponseWrapper.success(response);
  }

  @Operation(
      summary = "Refresh Token",
      description = "Issues a new access token using a valid refresh token."
  )
  @PostMapping(value = "/refresh-token", produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseWrapper<RefreshTokenResponse> refreshToken(
      @Parameter(description = "Refresh token sent in the request body", required = true)
      @Valid @RequestBody RefreshTokenRequest request) throws Exception {
    RefreshTokenResponse response = authService.refreshToken(request);
    return ApiResponseWrapper.success(response);
  }

  @Operation(
      summary = "User Logout",
      description = "Logs out the user and clears authentication cookies."
  )
  @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseWrapper<Void> logout() throws Exception {
    authService.logout();
    return ApiResponseWrapper.success();
  }
}

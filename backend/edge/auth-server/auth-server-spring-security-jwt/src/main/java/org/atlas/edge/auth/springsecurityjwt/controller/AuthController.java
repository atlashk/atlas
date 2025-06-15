package org.atlas.edge.auth.springsecurityjwt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.atlas.edge.auth.springsecurityjwt.model.GenerateOneTimeTokenRequest;
import org.atlas.edge.auth.springsecurityjwt.model.GenerateOneTimeTokenResponse;
import org.atlas.edge.auth.springsecurityjwt.model.LoginRequest;
import org.atlas.edge.auth.springsecurityjwt.model.LoginResponse;
import org.atlas.edge.auth.springsecurityjwt.model.OneTimeTokenLoginRequest;
import org.atlas.edge.auth.springsecurityjwt.model.RefreshTokenRequest;
import org.atlas.edge.auth.springsecurityjwt.model.RefreshTokenResponse;
import org.atlas.edge.auth.springsecurityjwt.service.AuthService;
import org.atlas.edge.auth.springsecurityjwt.service.CookieService;
import org.atlas.framework.api.server.rest.response.ApiResponseWrapper;
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
  private final CookieService cookieService;

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

  @Operation(
      summary = "Force logout on all devices",
      description = "Logs out the user and clears authentication cookies."
  )
  @PostMapping(value = "/logout/force", produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseWrapper<Void> forceLogoutOnAllDevices() {
    authService.forceLogoutOnAllDevices();
    return ApiResponseWrapper.success();
  }
}

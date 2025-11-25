package org.atlas.infrastructure.api.server.rest.impl.auth.authentication.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.auth.usecase.authentication.handler.GenerateOneTimeTokenUseCase;
import org.atlas.domain.auth.usecase.authentication.handler.LoginUseCase;
import org.atlas.domain.auth.usecase.authentication.handler.LogoutUseCase;
import org.atlas.domain.auth.usecase.authentication.handler.OneTimeTokenLoginUseCase;
import org.atlas.domain.auth.usecase.authentication.handler.RefreshTokenUseCase;
import org.atlas.domain.auth.usecase.authentication.model.GenerateOneTimeTokenInput;
import org.atlas.domain.auth.usecase.authentication.model.GenerateOneTimeTokenOutput;
import org.atlas.domain.auth.usecase.authentication.model.LoginInput;
import org.atlas.domain.auth.usecase.authentication.model.LoginOutput;
import org.atlas.domain.auth.usecase.authentication.model.OneTimeTokenLoginInput;
import org.atlas.domain.auth.usecase.authentication.model.RefreshTokenInput;
import org.atlas.domain.auth.usecase.authentication.model.RefreshTokenOutput;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.util.StringUtil;
import org.atlas.infrastructure.api.server.rest.impl.auth.authentication.mapper.AuthenticationMapper;
import org.atlas.infrastructure.api.server.rest.impl.auth.authentication.model.GenerateOneTimeTokenRequest;
import org.atlas.infrastructure.api.server.rest.impl.auth.authentication.model.GenerateOneTimeTokenResponse;
import org.atlas.infrastructure.api.server.rest.impl.auth.authentication.model.LoginRequest;
import org.atlas.infrastructure.api.server.rest.impl.auth.authentication.model.LoginResponse;
import org.atlas.infrastructure.api.server.rest.impl.auth.authentication.model.OneTimeTokenLoginRequest;
import org.atlas.infrastructure.api.server.rest.impl.auth.authentication.model.RefreshTokenRequest;
import org.atlas.infrastructure.api.server.rest.impl.auth.authentication.model.RefreshTokenResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

  private final LoginUseCase loginUseCase;
  private final OneTimeTokenLoginUseCase oneTimeTokenLoginUseCase;
  private final GenerateOneTimeTokenUseCase generateOneTimeTokenUseCase;
  private final RefreshTokenUseCase refreshTokenUseCase;
  private final LogoutUseCase logoutUseCase;

  @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "User login")
  public ApiResponseWrapper<LoginResponse> login(
      @Parameter(description = "Request object containing user credentials for login", required = true)
      @Valid @RequestBody LoginRequest request) throws Exception {
    LoginInput input = AuthenticationMapper.INSTANCE.toLoginInput(request);
    LoginOutput output = loginUseCase.handle(input);
    LoginResponse response = AuthenticationMapper.INSTANCE.toLoginResponse(output);
    return ApiResponseWrapper.success(response);
  }

  @PostMapping("/ott/login")
  @Operation(summary = "One-time token login")
  public ApiResponseWrapper<LoginResponse> oneTimeTokenLogin(
      @Valid @RequestBody OneTimeTokenLoginRequest request) throws Exception {
    OneTimeTokenLoginInput input = AuthenticationMapper.INSTANCE.toOneTimeTokenLoginInput(request);
    LoginOutput output = oneTimeTokenLoginUseCase.handle(input);
    LoginResponse response = AuthenticationMapper.INSTANCE.toLoginResponse(output);
    return ApiResponseWrapper.success(response);
  }

  @PostMapping("/ott/generate")
  @Operation(summary = "Generate one-time token")
  public ApiResponseWrapper<GenerateOneTimeTokenResponse> generateOneTimeToken(
      @Valid @RequestBody GenerateOneTimeTokenRequest request) {
    GenerateOneTimeTokenInput input = AuthenticationMapper.INSTANCE.toGenerateOneTimeTokenInput(
        request);
    GenerateOneTimeTokenOutput output = generateOneTimeTokenUseCase.handle(input);
    GenerateOneTimeTokenResponse response = AuthenticationMapper.INSTANCE.toGenerateOneTimeTokenResponse(
        output);
    return ApiResponseWrapper.success(response);
  }

  @PostMapping(value = "/refresh-token", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Refresh token")
  public ApiResponseWrapper<RefreshTokenResponse> refreshToken(
      @Parameter(description = "Refresh token sent in the request body", required = true)
      @Valid @RequestBody RefreshTokenRequest request) throws Exception {
    RefreshTokenInput input = AuthenticationMapper.INSTANCE.toRefreshTokenInput(request);
    RefreshTokenOutput output = refreshTokenUseCase.handle(input);
    RefreshTokenResponse response = AuthenticationMapper.INSTANCE.toRefreshTokenResponse(output);
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
    logoutUseCase.handle(accessToken);
    return ApiResponseWrapper.success();
  }
}

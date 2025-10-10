package org.atlas.infrastructure.api.server.rest.impl.user.front.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.UserEntity;
import org.atlas.domain.user.usecase.front.handler.GetProfileUseCaseHandler;
import org.atlas.domain.user.usecase.front.handler.RegisterUseCaseHandler;
import org.atlas.domain.user.usecase.front.model.GetProfileInput;
import org.atlas.domain.user.usecase.front.model.RegisterInput;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.context.Contexts;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.rest.impl.user.front.model.ProfileResponse;
import org.atlas.infrastructure.api.server.rest.impl.user.front.model.RegisterRequest;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/front/users")
@Validated
@RequiredArgsConstructor
public class UserController {

  private final RegisterUseCaseHandler registerUseCaseHandler;
  private final GetProfileUseCaseHandler getProfileUseCaseHandler;

  @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "User registration")
  public ApiResponseWrapper<Void> register(
      @Parameter(description = "Request object containing the needed information to register a user", required = true)
      @Valid @RequestBody RegisterRequest request) throws Exception {
    RegisterInput input = ObjectMapperUtil.getInstance()
        .map(request, RegisterInput.class);
    registerUseCaseHandler.handle(input);
    return ApiResponseWrapper.success();
  }

  @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get user profile")
  public ApiResponseWrapper<ProfileResponse> getProfile() throws Exception {
    GetProfileInput input = GetProfileInput.builder()
        .userId(Contexts.getUserId())
        .build();
    UserEntity user = getProfileUseCaseHandler.handle(input);
    ProfileResponse profileResponse = ObjectMapperUtil.getInstance()
        .map(user, ProfileResponse.class);
    return ApiResponseWrapper.success(profileResponse);
  }
}

package org.atlas.infrastructure.api.server.rest.impl.user.front.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.User;
import org.atlas.domain.user.usecase.front.handler.GetProfileUseCaseHandler;
import org.atlas.domain.user.usecase.front.handler.CreateUserUseCaseHandler;
import org.atlas.domain.user.usecase.front.model.CreateUserInput;
import org.atlas.domain.user.usecase.front.model.GetProfileInput;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.context.Contexts;
import org.atlas.infrastructure.api.server.rest.impl.user.front.mapper.UserMapper;
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
@RequestMapping("/api/users")
@Validated
@RequiredArgsConstructor
public class UserController {

  private final CreateUserUseCaseHandler createUserUseCaseHandler;
  private final GetProfileUseCaseHandler getProfileUseCaseHandler;

  @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "User registration")
  public ApiResponseWrapper<Void> register(
      @Parameter(description = "Request object containing the needed information to register a user", required = true)
      @Valid @RequestBody RegisterRequest request) throws Exception {
    CreateUserInput input = UserMapper.INSTANCE.toRegisterInput(request);
    createUserUseCaseHandler.handle(input);
    return ApiResponseWrapper.success();
  }

  @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get user profile")
  public ApiResponseWrapper<ProfileResponse> getProfile() throws Exception {
    GetProfileInput input = GetProfileInput.builder()
        .userId(Contexts.getUserId())
        .build();
    User user = getProfileUseCaseHandler.handle(input);
    ProfileResponse response = UserMapper.INSTANCE.toProfileResponse(user);
    return ApiResponseWrapper.success(response);
  }
}

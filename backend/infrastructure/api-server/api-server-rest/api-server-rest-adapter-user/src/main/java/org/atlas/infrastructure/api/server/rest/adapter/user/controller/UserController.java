package org.atlas.infrastructure.api.server.rest.adapter.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.UserEntity;
import org.atlas.domain.user.usecase.common.handler.GetProfileUseCaseHandler;
import org.atlas.domain.user.usecase.front.handler.FrontRegisterUseCaseHandler;
import org.atlas.domain.user.usecase.front.model.RegisterInput;
import org.atlas.framework.api.server.rest.response.ApiResponseWrapper;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.rest.adapter.user.model.RegisterRequest;
import org.atlas.infrastructure.api.server.rest.adapter.user.model.UserResponse;
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

  private final GetProfileUseCaseHandler getProfileUseCaseHandler;
  private final FrontRegisterUseCaseHandler frontRegisterUseCaseHandler;

  @Operation(
      summary = "Get User Profile",
      description = "Retrieves the profile information of the authenticated user."
  )
  @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseWrapper<UserResponse> getProfile() throws Exception {
    UserEntity userEntity = getProfileUseCaseHandler.handle(null);
    UserResponse userResponse = ObjectMapperUtil.getInstance()
        .map(userEntity, UserResponse.class);
    return ApiResponseWrapper.success(userResponse);
  }

  @Operation(summary = "User registration", description = "Registers a new user with the provided details.")
  @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseWrapper<Void> register(
      @Parameter(description = "Request object containing the needed information to register a user.", required = true)
      @Valid @RequestBody RegisterRequest request) throws Exception {
    RegisterInput input = ObjectMapperUtil.getInstance()
        .map(request, RegisterInput.class);
    frontRegisterUseCaseHandler.handle(input);
    return ApiResponseWrapper.success();
  }
}

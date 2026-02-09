package org.atlas.services.iam.infrastructure.api.server.rest.front.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.services.iam.infrastructure.api.server.rest.front.mapper.UserMapper;
import org.atlas.services.iam.infrastructure.api.server.rest.front.model.ChangePasswordRequest;
import org.atlas.services.iam.infrastructure.api.server.rest.front.model.ProfileResponse;
import org.atlas.services.iam.infrastructure.api.server.rest.front.model.RegisterRequest;
import org.atlas.services.iam.port.in.front.model.ChangePasswordInput;
import org.atlas.services.iam.port.in.front.model.ProfileOutput;
import org.atlas.services.iam.port.in.front.model.RegisterInput;
import org.atlas.services.iam.port.in.front.service.UserService;
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

  private final UserService userService;

  @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "User registration")
  public ApiResponseWrapper<Void> register(
      @Parameter(description = "Request object containing the needed information to register a user", required = true)
      @Valid @RequestBody RegisterRequest request) {
    RegisterInput input = UserMapper.INSTANCE.toRegisterInput(request);
    userService.register(input);
    return ApiResponseWrapper.success();
  }

  @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve user profile")
  public ApiResponseWrapper<ProfileResponse> retrieveProfile() {
    ProfileOutput output = userService.retrieveProfile();
    ProfileResponse response = UserMapper.INSTANCE.toProfileResponse(output);
    return ApiResponseWrapper.success(response);
  }

  @PostMapping(value = "/change-password", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Change user password")
  public ApiResponseWrapper<Void> changePassword(
      @Parameter(description = "Request object containing the needed information to change user password", required = true)
      @Valid @RequestBody ChangePasswordRequest request) {
    ChangePasswordInput input = UserMapper.INSTANCE.toChangePasswordInput(request);
    userService.changePassword(input);
    return ApiResponseWrapper.success();
  }
}

package org.atlas.services.user.api.server.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.context.Contexts;
import org.atlas.services.user.api.server.rest.mapper.UserMapper;
import org.atlas.services.user.api.server.rest.model.ProfileResponse;
import org.atlas.services.user.api.server.rest.model.RegisterRequest;
import org.atlas.services.user.application.model.CreateUserInput;
import org.atlas.services.user.application.service.UserService;
import org.atlas.services.user.domain.entity.User;
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

  private final UserService userService;

  @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "User registration")
  public ApiResponseWrapper<Void> register(
      @Parameter(description = "Request object containing the needed information to register a user", required = true)
      @Valid @RequestBody RegisterRequest request) {
    CreateUserInput input = UserMapper.INSTANCE.toCreateUserInput(request);
    userService.createUser(input);
    return ApiResponseWrapper.success();
  }

  @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve user profile")
  public ApiResponseWrapper<ProfileResponse> retrieveUserProfile() {
    User user = userService.retrieveUser(Contexts.getUserId());
    ProfileResponse response = UserMapper.INSTANCE.toProfileResponse(user);
    return ApiResponseWrapper.success(response);
  }
}

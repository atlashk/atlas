package org.atlas.platform.authorization.spring.api.rest.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.rest.ApiResponseWrapper;
import org.atlas.platform.authorization.spring.api.rest.user.mapper.UserMapper;
import org.atlas.platform.authorization.spring.api.rest.user.model.ProfileResponse;
import org.atlas.platform.authorization.spring.api.rest.user.model.RegisterRequest;
import org.atlas.platform.authorization.port.in.user.model.ProfileOutput;
import org.atlas.platform.authorization.port.in.user.model.RegisterInput;
import org.atlas.platform.authorization.port.in.user.service.UserService;
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
    RegisterInput input = UserMapper.INSTANCE.toRegisterInput(request);
    userService.register(input);
    return ApiResponseWrapper.success();
  }

  @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve user profile")
  public ApiResponseWrapper<ProfileResponse> retrieveProfile() {
    ProfileOutput output = userService.retrieveProfile();
    ProfileResponse responseData = UserMapper.INSTANCE.toProfileResponse(output);
    return ApiResponseWrapper.success(responseData);
  }
}

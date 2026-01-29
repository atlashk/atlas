package org.atlas.platform.auth.jwt.api.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.platform.auth.jwt.api.internal.model.CreateUserRequest;
import org.atlas.platform.auth.jwt.api.internal.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/internal/users")
@Validated
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "User creation")
  public ApiResponseWrapper<Void> createUser(
      @Parameter(description = "Request object containing the needed information to create a user", required = true)
      @Valid @RequestBody CreateUserRequest request) throws Exception {
    userService.createUser(request);
    return ApiResponseWrapper.success();
  }
}

package org.atlas.edge.auth.springsecurityjwt.api.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.atlas.edge.auth.springsecurityjwt.api.internal.model.CreateUserRequest;
import org.atlas.edge.auth.springsecurityjwt.api.internal.service.UserService;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
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

  @PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "User creation")
  public ApiResponseWrapper<Void> createUser(
      @Parameter(description = "Request object containing the needed information to create a user", required = true)
      @Valid @RequestBody CreateUserRequest request) throws Exception {
    userService.createUser(request);
    return ApiResponseWrapper.success();
  }
}

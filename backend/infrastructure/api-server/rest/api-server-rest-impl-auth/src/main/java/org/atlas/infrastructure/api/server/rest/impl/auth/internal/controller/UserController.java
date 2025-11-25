package org.atlas.infrastructure.api.server.rest.impl.auth.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.auth.usecase.user.handler.CreateUserUseCaseHandler;
import org.atlas.domain.auth.usecase.user.model.CreateUserInput;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.infrastructure.api.server.rest.impl.auth.internal.mapper.UserMapper;
import org.atlas.infrastructure.api.server.rest.impl.auth.internal.model.CreateUserRequest;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/users")
@Validated
@RequiredArgsConstructor
public class UserController {

  private final CreateUserUseCaseHandler createUserUseCaseHandler;

  @PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "User creation")
  public ApiResponseWrapper<Void> register(
      @Parameter(description = "Request object containing the needed information to create a user", required = true)
      @Valid @RequestBody CreateUserRequest request) throws Exception {
    CreateUserInput input = UserMapper.INSTANCE.toCreateUserInput(request);
    createUserUseCaseHandler.handle(input);
    return ApiResponseWrapper.success();
  }
}

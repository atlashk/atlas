package org.atlas.infrastructure.api.server.rest.adapter.user.front.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.usecase.front.handler.FrontRegisterUseCaseHandler;
import org.atlas.domain.user.usecase.front.model.RegisterInput;
import org.atlas.framework.api.server.rest.response.ApiResponseWrapper;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.rest.adapter.user.front.model.RegisterRequest;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/front/users")
@Validated
@RequiredArgsConstructor
public class FrontUserController {

  private final FrontRegisterUseCaseHandler frontRegisterUseCaseHandler;

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

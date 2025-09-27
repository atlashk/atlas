package org.atlas.infrastructure.api.server.rest.impl.user.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.UserEntity;
import org.atlas.domain.user.usecase.common.handler.GetProfileUseCaseHandler;
import org.atlas.domain.user.usecase.front.handler.FrontRegisterUseCaseHandler;
import org.atlas.domain.user.usecase.front.model.RegisterInput;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.rest.impl.user.model.RegisterRequest;
import org.atlas.infrastructure.api.server.rest.impl.user.model.UserResponse;
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
public class CommonUserController {

  private final GetProfileUseCaseHandler getProfileUseCaseHandler;

  @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get user profile")
  public ApiResponseWrapper<UserResponse> getProfile() throws Exception {

    UserEntity userEntity = getProfileUseCaseHandler.handle(null);
    UserResponse userResponse = ObjectMapperUtil.getInstance()
        .map(userEntity, UserResponse.class);
    return ApiResponseWrapper.success(userResponse);
  }
}

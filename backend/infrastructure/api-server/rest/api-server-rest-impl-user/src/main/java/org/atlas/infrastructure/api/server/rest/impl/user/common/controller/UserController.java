package org.atlas.infrastructure.api.server.rest.impl.user.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.UserEntity;
import org.atlas.domain.user.usecase.common.handler.GetProfileUseCaseHandler;
import org.atlas.domain.user.usecase.common.model.GetProfileInput;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.context.Contexts;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.rest.impl.user.common.model.UserResponse;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@Validated
@RequiredArgsConstructor
public class UserController {

  private final GetProfileUseCaseHandler getProfileUseCaseHandler;

  @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get user profile")
  public ApiResponseWrapper<UserResponse> getProfile() throws Exception {
    GetProfileInput input = GetProfileInput.builder()
        .userId(Contexts.getUserId())
        .build();
    UserEntity user = getProfileUseCaseHandler.handle(input);
    UserResponse userResponse = ObjectMapperUtil.getInstance()
        .map(user, UserResponse.class);
    return ApiResponseWrapper.success(userResponse);
  }
}

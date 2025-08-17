package org.atlas.infrastructure.api.server.rest.adapter.user.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.UserEntity;
import org.atlas.domain.user.usecase.common.handler.GetProfileUseCaseHandler;
import org.atlas.framework.api.server.rest.response.ApiResponseWrapper;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.rest.adapter.user.shared.UserResponse;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/common/users/profile")
@Validated
@RequiredArgsConstructor
public class CommonUserController {

  private final GetProfileUseCaseHandler getProfileUseCaseHandler;

  @Operation(
      summary = "Get User Profile",
      description = "Retrieves the profile information of the authenticated user."
  )
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseWrapper<UserResponse> getProfile() throws Exception {
    UserEntity userEntity = getProfileUseCaseHandler.handle(null);
    UserResponse userResponse = ObjectMapperUtil.getInstance()
        .map(userEntity, UserResponse.class);
    return ApiResponseWrapper.success(userResponse);
  }
}

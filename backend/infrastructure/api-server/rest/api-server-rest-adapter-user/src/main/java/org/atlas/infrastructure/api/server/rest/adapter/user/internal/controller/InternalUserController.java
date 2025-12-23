package org.atlas.infrastructure.api.server.rest.adapter.user.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.application.user.internal.model.InternalRetrieveUserListInput;
import org.atlas.application.user.internal.service.InternalUserService;
import org.atlas.domain.user.entity.User;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.internalapi.user.model.UserResponse;
import org.atlas.framework.util.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.rest.adapter.user.internal.mapper.InternalUserMapper;
import org.atlas.infrastructure.api.server.rest.adapter.user.internal.model.InternalRetrieveUserListRequest;
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
public class InternalUserController {

  private final InternalUserService internalUserService;

  @PostMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a list of users based on specified criteria")
  public ApiResponseWrapper<List<UserResponse>> listUser(
      @Parameter(description = "Request object containing the criteria for listing users", required = true)
      @Valid @RequestBody InternalRetrieveUserListRequest request) {
    InternalRetrieveUserListInput input = InternalUserMapper.INSTANCE
        .toInternalRetrieveUserListInput(request);
    List<User> users = internalUserService.retrieveUserList(input);
    List<UserResponse> profileResponse = ObjectMapperUtil.mapList(users,
        InternalUserMapper.INSTANCE::toUserResponse);
    return ApiResponseWrapper.success(profileResponse);
  }
}

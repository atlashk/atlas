package org.atlas.services.user.api.server.rest.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.internalapi.user.model.UserResponse;
import org.atlas.libs.framework.util.ObjectMapperUtil;
import org.atlas.services.user.api.server.rest.internal.mapper.InternalUserMapper;
import org.atlas.services.user.api.server.rest.internal.model.InternalRetrieveUserListRequest;
import org.atlas.services.user.application.internal.model.InternalRetrieveUserListInput;
import org.atlas.services.user.application.internal.service.InternalUserService;
import org.atlas.services.user.domain.entity.User;
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

package org.atlas.services.identity.api.rest.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.internal.identity.model.RetrieveUserListInput;
import org.atlas.libs.framework.internal.identity.model.UserOutput;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.identity.api.rest.user.mapper.UserInternalMapper;
import org.atlas.services.identity.api.rest.user.model.internal.RetrieveUserListRequest;
import org.atlas.services.identity.api.rest.user.model.internal.UserResponse;
import org.atlas.services.identity.port.in.user.service.UserInternalService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/internal")
@Validated
@RequiredArgsConstructor
public class UserInternalController {

  private final UserInternalService userInternalService;

  @PostMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a list of users based on specified criteria")
  public ApiResponseWrapper<List<UserResponse>> retrieveUserList(
      @Parameter(description = "Request object containing the criteria for listing users", required = true)
      @Valid @RequestBody RetrieveUserListRequest request) {
    RetrieveUserListInput input = UserInternalMapper.INSTANCE.toRetrieveUserListInput(request);
    List<UserOutput> output = userInternalService.retrieveUserList(input);
    List<UserResponse> responseData = MapperUtil.mapList(output,
        UserInternalMapper.INSTANCE::toUserResponse);
    return ApiResponseWrapper.success(responseData);
  }
}

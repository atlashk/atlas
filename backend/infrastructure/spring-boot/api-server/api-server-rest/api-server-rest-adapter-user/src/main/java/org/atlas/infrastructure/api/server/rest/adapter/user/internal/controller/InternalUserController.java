package org.atlas.infrastructure.api.server.rest.adapter.user.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.UserEntity;
import org.atlas.domain.user.usecase.internal.handler.InternalListUserUseCaseHandler;
import org.atlas.domain.user.usecase.internal.model.InternalListUserInput;
import org.atlas.framework.api.server.rest.response.ApiResponseWrapper;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.rest.adapter.user.internal.model.InternalListUserRequest;
import org.atlas.infrastructure.api.server.rest.adapter.user.shared.UserResponse;
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
public class InternalUserController {

  private final InternalListUserUseCaseHandler internalListUserUseCaseHandler;

  @PostMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "List users", description = "Retrieves a list of users based on the provided user IDs.")
  public ApiResponseWrapper<List<UserResponse>> listUser(
      @Parameter(description = "Request object containing the user IDs for the user list.", required = true)
      @Valid @RequestBody InternalListUserRequest request)
      throws Exception {
    InternalListUserInput input = ObjectMapperUtil.getInstance()
        .map(request, InternalListUserInput.class);
    List<UserEntity> userEntities = internalListUserUseCaseHandler.handle(input);
    List<UserResponse> userResponses = ObjectMapperUtil.getInstance()
        .mapList(userEntities, UserResponse.class);
    return ApiResponseWrapper.success(userResponses);
  }
}

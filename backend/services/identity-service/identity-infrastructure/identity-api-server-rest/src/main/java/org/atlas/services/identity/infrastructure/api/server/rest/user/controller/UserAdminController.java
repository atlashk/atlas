package org.atlas.services.identity.infrastructure.api.server.rest.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.identity.infrastructure.api.server.rest.user.mapper.UserAdminMapper;
import org.atlas.services.identity.infrastructure.api.server.rest.user.model.admin.CreateUserRequest;
import org.atlas.services.identity.infrastructure.api.server.rest.user.model.admin.RetrieveUserListRequest;
import org.atlas.services.identity.infrastructure.api.server.rest.user.model.admin.UpdateUserRequest;
import org.atlas.services.identity.infrastructure.api.server.rest.user.model.admin.UserResponse;
import org.atlas.services.identity.port.in.user.model.admin.CreateUserInput;
import org.atlas.services.identity.port.in.user.model.admin.RetrieveUserListInput;
import org.atlas.services.identity.port.in.user.model.admin.UpdateUserInput;
import org.atlas.services.identity.port.in.user.model.admin.UserOutput;
import org.atlas.services.identity.port.in.user.service.UserAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/admin")
@RequiredArgsConstructor
public class UserAdminController {

  private final UserAdminService userAdminService;

  @PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a paginated list of users with optional filtering and pagination")
  public ApiResponseWrapper<List<UserResponse>> retrieveUserList(
      @Parameter(description = "Request object containing filters and pagination", required = true)
      @Valid @RequestBody RetrieveUserListRequest request
  ) throws Exception {
    RetrieveUserListInput input = UserAdminMapper.INSTANCE.toRetrieveUserListAdminInput(request);
    input.setPagingRequest(PagingRequest.of(request.getPage() - 1, request.getSize()));
    PagingResult<UserOutput> userPage = userAdminService.retrieveUserList(input);
    PagingResult<UserResponse> responseData = MapperUtil.mapPage(userPage,
        UserAdminMapper.INSTANCE::toUserResponse);
    return ApiResponseWrapper.successPage(responseData);
  }

  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a user by ID")
  public ApiResponseWrapper<UserResponse> retrieveUser(
      @Parameter(name = "id", description = "The unique identifier of the user to retrieve", example = "USR0000001")
      @PathVariable String id) {
    UserOutput output = userAdminService.retrieveUser(id);
    UserResponse responseData = UserAdminMapper.INSTANCE.toUserResponse(output);
    return ApiResponseWrapper.success(responseData);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a new user")
  public ApiResponseWrapper<Void> createUser(
      @Parameter(description = "Request object containing user details", required = true)
      @Valid @RequestBody CreateUserRequest request) {
    CreateUserInput input = UserAdminMapper.INSTANCE.toCreateUserInput(request);
    userAdminService.createUser(input);
    return ApiResponseWrapper.success();
  }

  @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Update an existing user by ID")
  public ApiResponseWrapper<Void> updateUser(
      @Parameter(name = "id", description = "The unique identifier of the user to update", example = "USR0000001")
      @PathVariable String id,
      @Parameter(description = "Request object containing updated user details", required = true)
      @Valid @RequestBody UpdateUserRequest request) {
    UpdateUserInput input = UserAdminMapper.INSTANCE.toUpdateUserInput(request, id);
    userAdminService.updateUser(input);
    return ApiResponseWrapper.success();
  }

  @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Delete a user by ID")
  public ApiResponseWrapper<Void> deleteUser(
      @Parameter(name = "id", description = "The unique identifier of the user to delete", example = "USR0000001")
      @PathVariable String id) {
    userAdminService.deleteUser(id);
    return ApiResponseWrapper.success();
  }

  @GetMapping("/statistics/count")
  @Operation(summary = "Retrieve the total user count")
  public ApiResponseWrapper<Long> retrieveTotalUserCount() throws Exception {
    Long responseData = userAdminService.retrieveTotalUserCount();
    return ApiResponseWrapper.success(responseData);
  }
}

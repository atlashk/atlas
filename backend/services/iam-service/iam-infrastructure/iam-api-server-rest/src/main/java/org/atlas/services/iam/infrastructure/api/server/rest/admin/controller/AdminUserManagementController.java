package org.atlas.services.iam.infrastructure.api.server.rest.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.iam.infrastructure.api.server.rest.admin.mapper.AdminUserMapper;
import org.atlas.services.iam.infrastructure.api.server.rest.admin.model.AdminCreateUserRequest;
import org.atlas.services.iam.infrastructure.api.server.rest.admin.model.AdminRetrieveUserListRequest;
import org.atlas.services.iam.infrastructure.api.server.rest.admin.model.AdminUpdateUserRequest;
import org.atlas.services.iam.infrastructure.api.server.rest.admin.model.AdminUserResponse;
import org.atlas.services.iam.port.in.admin.model.AdminCreateUserInput;
import org.atlas.services.iam.port.in.admin.model.AdminRetrieveUserListInput;
import org.atlas.services.iam.port.in.admin.model.AdminUpdateUserInput;
import org.atlas.services.iam.port.in.admin.model.AdminUserOutput;
import org.atlas.services.iam.port.in.admin.service.AdminUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("/api/admin/users")
@Validated
@RequiredArgsConstructor
public class AdminUserManagementController {

  private final AdminUserService adminUserService;

  @PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a paginated list of users with optional filtering and pagination")
  public ApiResponseWrapper<List<AdminUserResponse>> retrieveUserList(
      @Parameter(description = "Request object containing filters and pagination", required = true)
      @Valid @RequestBody AdminRetrieveUserListRequest request
  ) throws Exception {
    AdminRetrieveUserListInput input = AdminUserMapper.INSTANCE
        .toAdminRetrieveUserListInput(request);
    input.setPagingRequest(PagingRequest.of(request.getPage() - 1, request.getSize()));
    
    PagingResult<AdminUserOutput> userPage = adminUserService.retrieveUserList(input);
    PagingResult<AdminUserResponse> responseData = MapperUtil.mapPage(userPage,
        AdminUserMapper.INSTANCE::toUserResponse);
    return ApiResponseWrapper.successPage(responseData);
  }

  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a user by ID")
  public ApiResponseWrapper<AdminUserResponse> retrieveUser(
      @Parameter(name = "id", description = "The unique identifier of the user to retrieve", example = "1")
      @PathVariable String id) {
    AdminUserOutput output = adminUserService.retrieveUser(id);
    AdminUserResponse response = AdminUserMapper.INSTANCE.toUserResponse(output);
    return ApiResponseWrapper.success(response);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a new user")
  public ApiResponseWrapper<Void> createUser(
      @Parameter(description = "Request object containing user details", required = true)
      @Valid @RequestBody AdminCreateUserRequest request) {
    AdminCreateUserInput input = AdminUserMapper.INSTANCE.toAdminCreateUserInput(request);

    adminUserService.createUser(input);
    return ApiResponseWrapper.success();
  }

  @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Update an existing user by ID")
  public ApiResponseWrapper<Void> updateUser(
      @Parameter(name = "id", description = "The unique identifier of the user to update", example = "1")
      @PathVariable String id,
      @Parameter(description = "Request object containing updated user details", required = true)
      @Valid @RequestBody AdminUpdateUserRequest request) {
    AdminUpdateUserInput input = AdminUserMapper.INSTANCE.toAdminUpdateUserInput(request);
    input.setId(id);

    adminUserService.updateUser(input);
    return ApiResponseWrapper.success();
  }

  @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Delete a user by ID")
  public ApiResponseWrapper<Void> deleteUser(
      @Parameter(name = "id", description = "The unique identifier of the user to delete", example = "1")
      @PathVariable String id) {
    adminUserService.deleteUser(id);
    return ApiResponseWrapper.success();
  }
}

package org.atlas.services.iam.infrastructure.api.server.rest.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.constant.CommonConstant;
import org.atlas.libs.framework.domain.user.UserRole;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.iam.infrastructure.api.server.rest.admin.mapper.AdminUserMapper;
import org.atlas.services.iam.infrastructure.api.server.rest.admin.model.AdminCreateUserRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@Validated
@RequiredArgsConstructor
public class AdminUserManagementController {

  private final AdminUserService adminUserService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a paginated list of users with optional filtering and pagination")
  public ApiResponseWrapper<List<AdminUserResponse>> listUser(
      @Parameter(name = "id", description = "ID", example = "1")
      @RequestParam(name = "id", required = false) String id,
      @Parameter(name = "username", description = "Username", example = "john.doe")
      @RequestParam(name = "username", required = false) String username,
      @Parameter(name = "firstName", description = "First name", example = "John")
      @RequestParam(name = "firstName", required = false) String firstName,
      @Parameter(name = "lastName", description = "Last name", example = "Doe")
      @RequestParam(name = "lastName", required = false) String lastName,
      @Parameter(name = "email", description = "Email", example = "johndoe@example.com")
      @RequestParam(name = "email", required = false) String email,
      @Parameter(name = "phoneNumber", description = "Phone number", example = "+1234567890")
      @RequestParam(name = "phoneNumber", required = false) String phoneNumber,
      @Parameter(name = "role", description = "User role", example = "USER")
      @RequestParam(name = "role", required = false) UserRole role,
      @Parameter(name = "page", description = "The page number", example = "1")
      @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
      @Parameter(name = "size", description = "The number of users per page", example = "20")
      @RequestParam(name = "size", required = false, defaultValue = CommonConstant.DEFAULT_PAGE_SIZE_STR) Integer size
  ) throws Exception {
    AdminRetrieveUserListInput input = AdminRetrieveUserListInput.builder()
        .id(id)
        .username(username)
        .firstName(firstName)
        .lastName(lastName)
        .email(email)
        .phoneNumber(phoneNumber)
        .role(role)
        .pagingRequest(PagingRequest.of(page - 1, size))
        .build();
    PagingResult<AdminUserOutput> userPage = adminUserService.retrieveUserList(input);

    PagingResult<AdminUserResponse> responseData = MapperUtil.mapPage(userPage,
        AdminUserMapper.INSTANCE::toUserResponse);
    return ApiResponseWrapper.successPage(responseData);
  }

  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a new user")
  public ApiResponseWrapper<Void> createUser(
      @Parameter(description = "Request object containing user details", required = true)
      @Valid @RequestBody AdminCreateUserRequest request) {
    AdminCreateUserInput input = AdminCreateUserInput.builder()
        .username(request.getUsername())
        .password(request.getPassword())
        .firstName(request.getFirstName())
        .lastName(request.getLastName())
        .email(request.getEmail())
        .phoneNumber(request.getPhoneNumber())
        .role(request.getRole())
        .build();
    adminUserService.createUser(input);
    return ApiResponseWrapper.success();
  }

  @PutMapping(value = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Update an existing user by ID")
  public ApiResponseWrapper<Void> updateUser(
      @Parameter(name = "userId", description = "The unique identifier of the user to update", example = "1")
      @PathVariable String userId,
      @Parameter(description = "Request object containing updated user details", required = true)
      @Valid @RequestBody AdminUpdateUserRequest request) {
    AdminUpdateUserInput input = AdminUpdateUserInput.builder()
        .userId(userId)
        .password(request.getPassword())
        .firstName(request.getFirstName())
        .lastName(request.getLastName())
        .role(request.getRole())
        .build();
    adminUserService.updateUser(input);
    return ApiResponseWrapper.success();
  }

  @DeleteMapping(value = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Delete a user by ID")
  public ApiResponseWrapper<Void> deleteUser(
      @Parameter(name = "userId", description = "The unique identifier of the user to delete", example = "1")
      @PathVariable String userId) {
    adminUserService.deleteUser(userId);
    return ApiResponseWrapper.success();
  }
}

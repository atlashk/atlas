package org.atlas.services.iam.infrastructure.api.server.rest.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.constant.CommonConstant;
import org.atlas.libs.framework.domain.user.UserRole;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.iam.infrastructure.api.server.rest.admin.mapper.AdminUserMapper;
import org.atlas.services.iam.infrastructure.api.server.rest.admin.model.AdminUserResponse;
import org.atlas.services.iam.port.in.admin.model.AdminRetrieveUserListInput;
import org.atlas.services.iam.port.in.admin.model.AdminUserOutput;
import org.atlas.services.iam.port.in.admin.service.AdminUserService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
      @Parameter(name = "userId", description = "User ID", example = "1")
      @RequestParam(name = "userId", required = false) String userId,
      @Parameter(name = "id", description = "User ID (legacy param)", example = "1")
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
    String effectiveUserId = userId != null ? userId : id;
    AdminRetrieveUserListInput input = AdminRetrieveUserListInput.builder()
        .userId(effectiveUserId)
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
}

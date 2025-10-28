package org.atlas.infrastructure.api.server.rest.impl.user.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.User;
import org.atlas.domain.user.shared.Role;
import org.atlas.domain.user.usecase.admin.handler.AdminListUserUseCaseHandler;
import org.atlas.domain.user.usecase.admin.model.AdminListUserInput;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.constant.CommonConstant;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingResult;
import org.atlas.framework.util.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.rest.impl.user.admin.mapper.AdminUserMapper;
import org.atlas.infrastructure.api.server.rest.impl.user.admin.model.UserResponse;
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

  private final AdminListUserUseCaseHandler adminListUserUseCaseHandler;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a paginated list of users with optional filtering and pagination")
  public ApiResponseWrapper<List<UserResponse>> listUser(
      @Parameter(name = "id", description = "User ID", example = "1")
      @RequestParam(name = "id", required = false) Integer id,
      @Parameter(name = "keyword", description = "Username, first name, last name, email, phone number", example = "john.doe")
      @RequestParam(name = "keyword", required = false) String keyword,
      @Parameter(name = "role", description = "User role", example = "USER")
      @RequestParam(name = "role", required = false) Role role,
      @Parameter(name = "page", description = "The page number", example = "1")
      @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
      @Parameter(name = "size", description = "The number of users per page", example = "20")
      @RequestParam(name = "size", required = false, defaultValue = CommonConstant.DEFAULT_PAGE_SIZE_STR) Integer size
  ) throws Exception {
    AdminListUserInput input = AdminListUserInput.builder()
        .id(id)
        .keyword(keyword)
        .role(role)
        .pagingRequest(PagingRequest.of(page - 1, size))
        .build();
    PagingResult<User> userPage = adminListUserUseCaseHandler.handle(input);

    PagingResult<UserResponse> responseData = ObjectMapperUtil.mapPage(userPage,
        AdminUserMapper.INSTANCE::toUserResponse);
    return ApiResponseWrapper.successPage(responseData);
  }
}

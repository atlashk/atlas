package org.atlas.services.user.api.server.rest.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.services.user.application.admin.service.AdminUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users/statistics")
@RequiredArgsConstructor
public class AdminUserStatisticsController {

  private final AdminUserService adminUserService;

  @GetMapping("/count")
  @Operation(summary = "Retrieve the user count")
  public ApiResponseWrapper<Long> retrieveUserCount() throws Exception {
    Long responseData = adminUserService.retrieveUserCount();

    return ApiResponseWrapper.success(responseData);
  }
}

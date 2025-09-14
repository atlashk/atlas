package org.atlas.infrastructure.api.server.rest.impl.user.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.usecase.admin.handler.AdminCountUserUseCaseHandler;
import org.atlas.framework.api.server.rest.response.ApiResponseWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users/statistics")
@RequiredArgsConstructor
@Tag(name = "Admin user statistics", description = "Admin user statistics")
public class AdminUserStatisticsController {

  private final AdminCountUserUseCaseHandler adminCountUserUseCaseHandler;

  @GetMapping("/count")
  @Operation(summary = "Count total users", description = "Get the total count of all users")
  public ApiResponseWrapper<Long> countUsers() throws Exception {
    Long count = adminCountUserUseCaseHandler.handle();
    return ApiResponseWrapper.success(count);
  }
}

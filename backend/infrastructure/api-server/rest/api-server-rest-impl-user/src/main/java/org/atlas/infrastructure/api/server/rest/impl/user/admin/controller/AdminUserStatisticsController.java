package org.atlas.infrastructure.api.server.rest.impl.user.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.usecase.admin.handler.AdminCountUserUseCaseHandler;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users/statistics")
@RequiredArgsConstructor
public class AdminUserStatisticsController {

  private final AdminCountUserUseCaseHandler adminCountUserUseCaseHandler;

  @GetMapping("/count")
  @Operation(summary = "Count total users")
  public ApiResponseWrapper<Long> countUsers() throws Exception {
    Long responseData = adminCountUserUseCaseHandler.handle();

    return ApiResponseWrapper.success(responseData);
  }
}

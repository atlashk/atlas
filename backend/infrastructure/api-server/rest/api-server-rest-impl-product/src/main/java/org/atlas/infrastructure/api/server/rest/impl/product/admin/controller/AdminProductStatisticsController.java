package org.atlas.infrastructure.api.server.rest.impl.product.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.usecase.admin.handler.AdminCountProductUseCaseHandler;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/products/statistics")
@Tag(name = "Admin product statistics", description = "Admin product statistics")
@RequiredArgsConstructor
public class AdminProductStatisticsController {

  private final AdminCountProductUseCaseHandler adminCountProductUseCaseHandler;

  @GetMapping("/count")
  @Operation(summary = "Count total products", description = "Get the total count of all products")
  public ApiResponseWrapper<Long> countProduct() throws Exception {
    Long count = adminCountProductUseCaseHandler.handle();
    return ApiResponseWrapper.success(count);
  }
}

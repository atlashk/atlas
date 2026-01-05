package org.atlas.product.api.server.rest.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.atlas.product.application.admin.service.AdminProductService;
import org.atlas.common.framework.api.server.rest.ApiResponseWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/products/statistics")
@RequiredArgsConstructor
public class AdminProductStatisticsController {

  private final AdminProductService adminProductService;

  @GetMapping("/count")
  @Operation(summary = "Retrieve the product count")
  public ApiResponseWrapper<Long> retrieveProductCount() {
    Long responseData = adminProductService.retrieveProductCount();
    return ApiResponseWrapper.success(responseData);
  }
}

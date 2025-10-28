package org.atlas.infrastructure.api.server.rest.impl.order.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.order.usecase.admin.handler.AdminCountOrderUseCaseHandler;
import org.atlas.domain.order.usecase.admin.handler.AdminGetTotalRevenueUseCaseHandler;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders/statistics")
@RequiredArgsConstructor
public class AdminOrderStatisticsController {

  private final AdminCountOrderUseCaseHandler adminCountOrderUseCaseHandler;
  private final AdminGetTotalRevenueUseCaseHandler adminGetTotalRevenueUseCaseHandler;

  @GetMapping("/count")
  @Operation(summary = "Count total orders")
  public ApiResponseWrapper<Long> countOrder() throws Exception {
    Long responseData = adminCountOrderUseCaseHandler.handle();

    return ApiResponseWrapper.success(responseData);
  }

  @GetMapping("/total-revenue")
  @Operation(summary = "Get total revenue")
  public ApiResponseWrapper<BigDecimal> getTotalRevenue() throws Exception {
    BigDecimal responseData = adminGetTotalRevenueUseCaseHandler.handle();

    return ApiResponseWrapper.success(responseData);
  }
}

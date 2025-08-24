package org.atlas.infrastructure.api.server.rest.adapter.order.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.order.usecase.admin.handler.AdminCountOrderUseCaseHandler;
import org.atlas.domain.order.usecase.admin.handler.AdminGetTotalRevenueUseCaseHandler;
import org.atlas.framework.api.server.rest.response.ApiResponseWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders/statistics")
@Tag(name = "Admin order statistics", description = "Admin order statistics")
@RequiredArgsConstructor
public class AdminOrderStatisticsController {

  private final AdminCountOrderUseCaseHandler adminCountOrderUseCaseHandler;
  private final AdminGetTotalRevenueUseCaseHandler adminGetTotalRevenueUseCaseHandler;

  @GetMapping("/count")
  @Operation(summary = "Count total orders", description = "Get the total count of all orders")
  public ApiResponseWrapper<Long> countOrder() throws Exception {
    Long count = adminCountOrderUseCaseHandler.handle();
    return ApiResponseWrapper.success(count);
  }

  @GetMapping("/total-revenue")
  @Operation(summary = "Get total revenue", description = "Get the total revenue of all confirmed orders")
  public ApiResponseWrapper<BigDecimal> getTotalRevenue() throws Exception {
    BigDecimal totalRevenue = adminGetTotalRevenueUseCaseHandler.handle();
    return ApiResponseWrapper.success(totalRevenue);
  }
}

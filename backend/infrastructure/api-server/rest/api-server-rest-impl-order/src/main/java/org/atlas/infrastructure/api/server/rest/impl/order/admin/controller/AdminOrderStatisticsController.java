package org.atlas.infrastructure.api.server.rest.impl.order.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.order.usecase.admin.handler.AdminCountOrderUseCaseHandler;
import org.atlas.domain.order.usecase.admin.handler.AdminGetTotalRevenueUseCaseHandler;
import org.atlas.domain.order.usecase.admin.handler.AdminGetMonthlyOrderStatisticsUseCaseHandler;
import org.atlas.domain.order.repository.model.MonthlyOrderAggregation;
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
  private final AdminGetMonthlyOrderStatisticsUseCaseHandler adminGetMonthlyOrderStatisticsUseCaseHandler;

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

  @GetMapping("/monthly")
  @Operation(summary = "Get monthly revenue")
  public ApiResponseWrapper<List<MonthlyOrderAggregation>> getMonthlyStatistics() throws Exception {
    List<MonthlyOrderAggregation> responseData =
        adminGetMonthlyOrderStatisticsUseCaseHandler.handle();

    return ApiResponseWrapper.success(responseData);
  }
}

package org.atlas.infrastructure.api.server.rest.adapter.order.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.application.order.admin.model.AdminMonthlyOrderAggregation;
import org.atlas.application.order.admin.service.AdminOrderService;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders/statistics")
@RequiredArgsConstructor
public class AdminOrderStatisticsController {

  private final AdminOrderService adminOrderService;

  @GetMapping("/count")
  @Operation(summary = "Retrieve order count")
  public ApiResponseWrapper<Long> retrieveOrderCount() {
    Long responseData = adminOrderService.retrieveOrderCount();
    return ApiResponseWrapper.success(responseData);
  }

  @GetMapping("/total-revenue")
  @Operation(summary = "Get total revenue")
  public ApiResponseWrapper<BigDecimal> retrieveTotalRevenue() {
    BigDecimal responseData = adminOrderService.retrieveTotalRevenue();
    return ApiResponseWrapper.success(responseData);
  }

  @GetMapping("/monthly")
  @Operation(summary = "Get monthly revenue")
  public ApiResponseWrapper<List<AdminMonthlyOrderAggregation>> retrieveMonthlyOrderStatistics() {
    List<AdminMonthlyOrderAggregation> responseData = adminOrderService.retrieveMonthlyOrderStatistics();
    return ApiResponseWrapper.success(responseData);
  }
}

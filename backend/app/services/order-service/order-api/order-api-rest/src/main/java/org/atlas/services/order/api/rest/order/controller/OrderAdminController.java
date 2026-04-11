package org.atlas.services.order.api.rest.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.rest.ApiResponseWrapper;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingRequest.SortOrder;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.order.api.rest.order.mapper.OrderAdminMapper;
import org.atlas.services.order.api.rest.order.model.admin.OrderResponse;
import org.atlas.services.order.api.rest.order.model.admin.RetrieveOrderListRequest;
import org.atlas.services.order.domain.entity.Order;
import org.atlas.services.order.port.in.order.model.admin.MonthlyOrderAggregation;
import org.atlas.services.order.port.in.order.model.admin.RetrieveOrderListInput;
import org.atlas.services.order.port.in.order.service.OrderAdminService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders/admin")
@Validated
@RequiredArgsConstructor
public class OrderAdminController {

  private final OrderAdminService orderAdminService;

  @PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a list of orders with optional filtering and pagination")
  public ApiResponseWrapper<List<OrderResponse>> retrieveOrderList(
      @Valid @RequestBody RetrieveOrderListRequest request) throws Exception {
    RetrieveOrderListInput input = OrderAdminMapper.INSTANCE.toRetrieveOrderListInput(request);
    input.setPagingRequest(PagingRequest.of(request.getPage() - 1, request.getSize(), "createdAt", SortOrder.DESC));
    PagingResult<Order> orders = orderAdminService.retrieveOrderList(input);
    PagingResult<OrderResponse> responseData = MapperUtil.mapPage(orders,
        OrderAdminMapper.INSTANCE::toOrderResponse);
    return ApiResponseWrapper.successPage(responseData);
  }

  @GetMapping(value = "/statistics/count", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve total order count")
  public ApiResponseWrapper<Long> retrieveTotalOrderCount() {
    Long responseData = orderAdminService.retrieveTotalOrderCount();
    return ApiResponseWrapper.success(responseData);
  }

  @GetMapping(value = "/statistics/total-revenue", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve total revenue")
  public ApiResponseWrapper<BigDecimal> retrieveTotalRevenue() {
    BigDecimal responseData = orderAdminService.retrieveTotalRevenue();
    return ApiResponseWrapper.success(responseData);
  }

  @GetMapping(value = "/statistics/monthly", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve monthly revenue")
  public ApiResponseWrapper<List<MonthlyOrderAggregation>> retrieveMonthlyOrderStatistics() {
    List<MonthlyOrderAggregation> responseData = orderAdminService.retrieveMonthlyOrderStatistics();
    return ApiResponseWrapper.success(responseData);
  }
}

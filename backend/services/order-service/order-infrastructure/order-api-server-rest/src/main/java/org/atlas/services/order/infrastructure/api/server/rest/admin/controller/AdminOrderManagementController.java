package org.atlas.services.order.infrastructure.api.server.rest.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.constant.CommonConstant;
import org.atlas.libs.framework.domain.order.OrderStatus;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingRequest.SortOrder;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.order.infrastructure.api.server.rest.admin.mapper.OrderMapper;
import org.atlas.services.order.infrastructure.api.server.rest.admin.model.AdminOrderResponse;
import org.atlas.services.order.port.in.admin.model.AdminOrderOutput;
import org.atlas.services.order.port.in.admin.model.AdminRetrieveOrderListInput;
import org.atlas.services.order.port.in.admin.service.AdminOrderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
@Validated
@RequiredArgsConstructor
public class AdminOrderManagementController {

  private final AdminOrderService adminOrderService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a list of orders with optional filtering and pagination")
  public ApiResponseWrapper<List<AdminOrderResponse>> retrieveOrderList(
      @Parameter(name = "orderId", description = "Order ID", example = "1")
      @RequestParam(name = "orderId", required = false) String orderId,
      @Parameter(name = "userId", description = "User ID", example = "1")
      @RequestParam(name = "userId", required = false) String userId,
      @Parameter(name = "productId", description = "Product ID", example = "1")
      @RequestParam(name = "productId", required = false) String productId,
      @Parameter(name = "status", description = "Order status")
      @RequestParam(name = "status", required = false) OrderStatus status,
      @Parameter(name = "startDate", description = "Start date")
      @RequestParam(name = "startDate", required = false)
      @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
      @Parameter(name = "endDate", description = "End date")
      @RequestParam(name = "endDate", required = false)
      @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
      @Parameter(name = "page", description = "The page number to be retrieved (default is 1)", example = "1")
      @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
      @Parameter(name = "size", description = "The number of orders per page (default is defined by the constant)", example = "20")
      @RequestParam(name = "size", required = false, defaultValue = CommonConstant.DEFAULT_PAGE_SIZE_STR) Integer size
  ) throws Exception {
    AdminRetrieveOrderListInput input = AdminRetrieveOrderListInput.builder()
        .id(orderId)
        .userId(userId)
        .productId(productId)
        .status(status)
        .startDate(startDate)
        .endDate(endDate)
        .pagingRequest(PagingRequest.of(page - 1, size, "createdAt", SortOrder.DESC))
        .build();
    PagingResult<AdminOrderOutput> output = adminOrderService.retrieveOrderList(input);
    PagingResult<AdminOrderResponse> responseData = MapperUtil.mapPage(output,
        OrderMapper.INSTANCE::toAdminOrderResponse);
    return ApiResponseWrapper.successPage(responseData);
  }
}

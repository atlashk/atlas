package org.atlas.infrastructure.api.server.rest.adapter.order.front.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.application.order.model.CheckoutInput;
import org.atlas.application.order.model.RetrieveOrderListInput;
import org.atlas.application.order.model.RetrieveOrderStatusOutput;
import org.atlas.application.order.service.OrderService;
import org.atlas.domain.order.entity.Order;
import org.atlas.domain.order.shared.OrderStatus;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.constant.CommonConstant;
import org.atlas.framework.context.Contexts;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingRequest.SortOrder;
import org.atlas.framework.paging.PagingResult;
import org.atlas.framework.util.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.rest.adapter.order.front.mapper.OrderMapper;
import org.atlas.infrastructure.api.server.rest.adapter.order.front.model.CheckoutRequest;
import org.atlas.infrastructure.api.server.rest.adapter.order.front.model.CheckoutResponse;
import org.atlas.infrastructure.api.server.rest.adapter.order.front.model.OrderResponse;
import org.atlas.infrastructure.api.server.rest.adapter.order.front.model.RetrieveOrderStatusResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@Validated
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a list of orders with optional filtering and pagination")
  public ApiResponseWrapper<List<OrderResponse>> retrieveOrderList(
      @Parameter(name = "status", description = "Order status")
      @RequestParam(name = "status", required = false) OrderStatus status,
      @Parameter(name = "startDate", description = "Start date")
      @RequestParam(name = "startDate", required = false)
      @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
      @Parameter(name = "endDate", description = "End date")
      @RequestParam(name = "endDate", required = false)
      @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
      @Parameter(name = "page", description = "The page number to retrieve (default is 1).", example = "1")
      @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
      @Parameter(name = "size", description = "The number of orders per page (default is defined by the constant).", example = "10")
      @RequestParam(name = "size", required = false, defaultValue = CommonConstant.DEFAULT_PAGE_SIZE_STR) Integer size
  ) {
    RetrieveOrderListInput input = RetrieveOrderListInput.builder()
        .userId(Contexts.getUserId())
        .status(status)
        .startDate(startDate)
        .endDate(endDate)
        .pagingRequest(PagingRequest.of(page - 1, size, "createdAt", SortOrder.DESC))
        .build();
    PagingResult<Order> orderPage = orderService.retrieveOrderList(input);
    PagingResult<OrderResponse> responseData = ObjectMapperUtil.mapPage(orderPage,
        OrderMapper.INSTANCE::toOrderResponse);
    return ApiResponseWrapper.successPage(responseData);
  }

  @PostMapping(value = "/checkout", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Checkout")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponseWrapper<CheckoutResponse> checkout(
      @Parameter(description = "Checkout request", required = true)
      @Valid @RequestBody CheckoutRequest request) {
    CheckoutInput input = OrderMapper.INSTANCE.toCheckoutInput(request);
    input.setUserId(Contexts.getUserId());
    Integer orderId = orderService.checkout(input);
    CheckoutResponse responseData = new CheckoutResponse(orderId);
    return ApiResponseWrapper.success(responseData);
  }

  @GetMapping(value = "/{orderId}/status", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get order status")
  public ApiResponseWrapper<RetrieveOrderStatusResponse> getOrderStatus(
      @Parameter(name = "orderId", description = "ID of the order to retrieve the status for", example = "123")
      @PathVariable Integer orderId) {
    RetrieveOrderStatusOutput output = orderService.retrieveOrderStatus(
        orderId, Contexts.getUserId());
    RetrieveOrderStatusResponse responseData = OrderMapper.INSTANCE.toGetOrderStatusResponse(
        output);
    return ApiResponseWrapper.success(responseData);
  }
}

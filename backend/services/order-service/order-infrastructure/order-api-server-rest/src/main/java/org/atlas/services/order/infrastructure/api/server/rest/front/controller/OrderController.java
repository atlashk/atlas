package org.atlas.services.order.infrastructure.api.server.rest.front.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.context.Contexts;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.order.domain.entity.OrderEntity;
import org.atlas.services.order.infrastructure.api.server.rest.front.mapper.OrderMapper;
import org.atlas.services.order.infrastructure.api.server.rest.front.model.CheckoutRequest;
import org.atlas.services.order.infrastructure.api.server.rest.front.model.CheckoutResponse;
import org.atlas.services.order.infrastructure.api.server.rest.front.model.OrderResponse;
import org.atlas.services.order.infrastructure.api.server.rest.front.model.RetrieveOrderListRequest;
import org.atlas.services.order.infrastructure.api.server.rest.front.model.RetrieveOrderStatusResponse;
import org.atlas.services.order.port.in.front.model.CheckoutInput;
import org.atlas.services.order.port.in.front.model.RetrieveOrderListInput;
import org.atlas.services.order.port.in.front.model.RetrieveOrderStatusOutput;
import org.atlas.services.order.port.in.front.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/front/orders")
@Validated
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a list of orders with optional filtering and pagination")
  public ApiResponseWrapper<List<OrderResponse>> retrieveOrderList(
      @Parameter(description = "Retrieve order list request", required = true)
      @Valid @RequestBody RetrieveOrderListRequest request) {
    RetrieveOrderListInput input = OrderMapper.INSTANCE.toRetrieveOrderListInput(request);
    input.setUserId(Contexts.getUserId());
    PagingResult<OrderEntity> orderPage = orderService.retrieveOrderList(input);
    PagingResult<OrderResponse> responseData = MapperUtil.mapPage(orderPage,
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
    String orderId = orderService.checkout(input);
    CheckoutResponse responseData = new CheckoutResponse(orderId);
    return ApiResponseWrapper.success(responseData);
  }

  @GetMapping(value = "/{id}/status", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get order status")
  public ApiResponseWrapper<RetrieveOrderStatusResponse> getOrderStatus(
      @Parameter(name = "id", description = "ID of the order to retrieve the status for", example = "123")
      @PathVariable String id) {
    RetrieveOrderStatusOutput output = orderService.retrieveOrderStatus(id);
    RetrieveOrderStatusResponse responseData = OrderMapper.INSTANCE.toGetOrderStatusResponse(
        output);
    return ApiResponseWrapper.success(responseData);
  }
}

package org.atlas.infrastructure.api.server.rest.adapter.order.front.controller;

import java.util.Date;
import java.util.List;

import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.shared.enums.OrderStatus;
import org.atlas.domain.order.usecase.front.handler.FrontGetOrderStatusUseCaseHandler;
import org.atlas.domain.order.usecase.front.handler.FrontListOrderUseCaseHandler;
import org.atlas.domain.order.usecase.front.handler.FrontPlaceOrderUseCaseHandler;
import org.atlas.domain.order.usecase.front.model.FrontGetOrderStatusOutput;
import org.atlas.domain.order.usecase.front.model.FrontListOrderInput;
import org.atlas.domain.order.usecase.front.model.FrontPlaceOrderInput;
import org.atlas.framework.api.server.rest.response.ApiResponseWrapper;
import org.atlas.framework.constant.CommonConstant;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingRequest.SortOrder;
import org.atlas.framework.paging.PagingResult;
import org.atlas.infrastructure.api.server.rest.adapter.order.front.model.FrontOrderStatusResponse;
import org.atlas.infrastructure.api.server.rest.adapter.order.front.model.FrontPlaceOrderRequest;
import org.atlas.infrastructure.api.server.rest.adapter.order.shared.OrderResponse;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/front/orders")
@Validated
@RequiredArgsConstructor
public class FrontOrderController {

  private final FrontListOrderUseCaseHandler frontListOrderUseCaseHandler;
  private final FrontGetOrderStatusUseCaseHandler frontGetOrderStatusUseCaseHandler;
  private final FrontPlaceOrderUseCaseHandler frontPlaceOrderUseCaseHandler;

  @Operation(summary = "List Orders", description = "Retrieves a paginated list of orders for the front-end.")
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseWrapper<List<OrderResponse>> listOrder(
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
  ) throws Exception {
    FrontListOrderInput input = FrontListOrderInput.builder()
        .startDate(startDate)
        .endDate(endDate)
        .status(status)
        .pagingRequest(PagingRequest.of(page - 1, size, "createdAt", SortOrder.DESC))
        .build();

    PagingResult<OrderEntity> orderEntityPage = frontListOrderUseCaseHandler.handle(input);

    PagingResult<OrderResponse> orderResponsePage = ObjectMapperUtil.getInstance()
        .mapPage(orderEntityPage, OrderResponse.class);
    return ApiResponseWrapper.successPage(orderResponsePage);
  }

  @Operation(summary = "Get Order Status", description = "Retrieves the status of a specific order by its ID.")
  @GetMapping(value = "/{orderId}/status", produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseWrapper<FrontOrderStatusResponse> getOrderStatus(
      @Parameter(name = "orderId", description = "ID of the order to retrieve the status for.", example = "123")
      @PathVariable("orderId") Integer orderId) throws Exception {
    FrontGetOrderStatusOutput output = frontGetOrderStatusUseCaseHandler.handle(orderId);
    FrontOrderStatusResponse response = ObjectMapperUtil.getInstance()
        .map(output, FrontOrderStatusResponse.class);
    return ApiResponseWrapper.success(response);
  }

  @Operation(summary = "Place Order", description = "Places a new order based on the provided order details.")
  @PostMapping(value = "/place", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponseWrapper<Integer> placeOrder(
      @Parameter(description = "Order details to create a new order.", required = true)
      @Valid @RequestBody FrontPlaceOrderRequest request) throws Exception {
    FrontPlaceOrderInput input = ObjectMapperUtil.getInstance()
        .map(request, FrontPlaceOrderInput.class);
    Integer orderId = frontPlaceOrderUseCaseHandler.handle(input);
    return ApiResponseWrapper.success(orderId);
  }
}

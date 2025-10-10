package org.atlas.infrastructure.api.server.rest.impl.order.front.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.shared.OrderStatus;
import org.atlas.domain.order.usecase.front.handler.CheckoutUseCaseHandler;
import org.atlas.domain.order.usecase.front.handler.ListOrderUseCaseHandler;
import org.atlas.domain.order.usecase.front.model.CheckoutInput;
import org.atlas.domain.order.usecase.front.model.ListOrderInput;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.constant.CommonConstant;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingRequest.SortOrder;
import org.atlas.framework.paging.PagingResult;
import org.atlas.infrastructure.api.server.rest.impl.order.front.model.CheckoutRequest;
import org.atlas.infrastructure.api.server.rest.impl.order.front.model.CheckoutResponse;
import org.atlas.infrastructure.api.server.rest.impl.order.front.model.OrderResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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

  private final ListOrderUseCaseHandler listOrderUseCaseHandler;
  private final CheckoutUseCaseHandler checkoutUseCaseHandler;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a list of orders with optional filtering and pagination")
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
    ListOrderInput input = ListOrderInput.builder()
        .status(status)
        .startDate(startDate)
        .endDate(endDate)
        .pagingRequest(PagingRequest.of(page - 1, size, "createdAt", SortOrder.DESC))
        .build();

    PagingResult<OrderEntity> orderPage = listOrderUseCaseHandler.handle(input);

    PagingResult<OrderResponse> orderResponsePage = ObjectMapperUtil.getInstance()
        .mapPage(orderPage, OrderResponse.class);
    return ApiResponseWrapper.successPage(orderResponsePage);
  }

  @PostMapping(value = "/checkout", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Checkout")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponseWrapper<CheckoutResponse> checkout(
      @Parameter(description = "Checkout request", required = true)
      @Valid @RequestBody CheckoutRequest request) throws Exception {
    CheckoutInput input = ObjectMapperUtil.getInstance()
        .map(request, CheckoutInput.class);

    Integer sagaId = checkoutUseCaseHandler.handle(input);

    CheckoutResponse response = CheckoutResponse.builder()
        .sagaId(sagaId)
        .build();
    return ApiResponseWrapper.success(response);
  }
}

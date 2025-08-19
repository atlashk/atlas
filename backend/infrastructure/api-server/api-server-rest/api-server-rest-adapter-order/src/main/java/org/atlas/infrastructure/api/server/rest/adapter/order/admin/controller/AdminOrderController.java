package org.atlas.infrastructure.api.server.rest.adapter.order.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.usecase.admin.handler.AdminListOrderUseCaseHandler;
import org.atlas.domain.order.usecase.admin.model.AdminListOrderInput;
import org.atlas.framework.api.server.rest.response.ApiResponseWrapper;
import org.atlas.framework.constant.CommonConstant;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingRequest.SortOrder;
import org.atlas.framework.paging.PagingResult;
import org.atlas.infrastructure.api.server.rest.adapter.order.shared.model.OrderResponse;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/admin/orders")
@Validated
@RequiredArgsConstructor
public class AdminOrderController {

  private final AdminListOrderUseCaseHandler adminListOrderUseCaseHandler;

  @Operation(summary = "List Orders", description = "Retrieves a paginated list of orders.")
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseWrapper<List<OrderResponse>> listOrder(
      @Parameter(name = "page", description = "The page number to be retrieved (default is 1).", example = "1")
      @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
      @Parameter(name = "size", description = "The number of orders per page (default is defined by the constant).", example = "20")
      @RequestParam(name = "size", required = false, defaultValue = CommonConstant.DEFAULT_PAGE_SIZE_STR) Integer size
  ) throws Exception {
    AdminListOrderInput input = AdminListOrderInput.builder()
        .pagingRequest(PagingRequest.of(page - 1, size, "createdAt", SortOrder.DESC))
        .build();

    PagingResult<OrderEntity> orderEntityPage = adminListOrderUseCaseHandler.handle(input);

    PagingResult<OrderResponse> orderResponsePage = ObjectMapperUtil.getInstance()
        .mapPage(orderEntityPage, OrderResponse.class);
    return ApiResponseWrapper.successPage(orderResponsePage);
  }
}

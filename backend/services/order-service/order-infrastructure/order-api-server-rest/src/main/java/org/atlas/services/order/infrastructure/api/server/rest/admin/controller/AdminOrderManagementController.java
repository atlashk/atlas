package org.atlas.services.order.infrastructure.api.server.rest.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.order.infrastructure.api.server.rest.admin.mapper.OrderMapper;
import org.atlas.services.order.infrastructure.api.server.rest.admin.model.AdminOrderResponse;
import org.atlas.services.order.infrastructure.api.server.rest.admin.model.AdminRetrieveOrderListRequest;
import org.atlas.services.order.port.in.admin.model.AdminOrderOutput;
import org.atlas.services.order.port.in.admin.model.AdminRetrieveOrderListInput;
import org.atlas.services.order.port.in.admin.service.AdminOrderService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
@Validated
@RequiredArgsConstructor
public class AdminOrderManagementController {

  private final AdminOrderService adminOrderService;

  @PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a list of orders with optional filtering and pagination")
  public ApiResponseWrapper<List<AdminOrderResponse>> retrieveOrderList(
      @Valid @RequestBody AdminRetrieveOrderListRequest request) throws Exception {
    AdminRetrieveOrderListInput input = OrderMapper.INSTANCE.toAdminRetrieveOrderListInput(request);

    PagingResult<AdminOrderOutput> output = adminOrderService.retrieveOrderList(input);
    PagingResult<AdminOrderResponse> responseData = MapperUtil.mapPage(output,
        OrderMapper.INSTANCE::toAdminOrderResponse);
    return ApiResponseWrapper.successPage(responseData);
  }
}

package org.atlas.services.inventory.infrastructure.api.server.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.internal.product.model.ProductOutput;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.inventory.infrastructure.api.server.rest.mapper.StockInternalMapper;
import org.atlas.services.inventory.infrastructure.api.server.rest.model.internal.RetrieveStockListRequest;
import org.atlas.services.product.port.in.internal.model.InternalRetrieveProductListInput;
import org.atlas.services.product.port.in.service.StockInternalService;
import org.atlas.services.inventory.domain.entity.StockEntity;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/products")
@Validated
@RequiredArgsConstructor
public class StockInternalController {

  private final StockInternalService stockInternalService;

  @PostMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a list of products based on specified criteria")
  public ApiResponseWrapper<List<ProductOutput>> retrieveProductList(
      @Parameter(description = "Request object containing the criteria for listing products", required = true)
      @Valid @RequestBody RetrieveStockListRequest request) {
    InternalRetrieveProductListInput input = StockInternalMapper.INSTANCE
        .toInternalRetrieveProductListInput(request);
    List<StockEntity> products = stockInternalService.retrieveProductList(input);
    List<ProductOutput> responseData = MapperUtil.mapList(products,
        StockInternalMapper.INSTANCE::toProductResponse);
    return ApiResponseWrapper.success(responseData);
  }
}

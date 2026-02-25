package org.atlas.services.inventory.api.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.services.inventory.api.rest.mapper.StockAdminMapper;
import org.atlas.services.inventory.api.rest.model.RetrieveStockResponse;
import org.atlas.services.inventory.api.rest.model.UpdateAvailableQuantityRequest;
import org.atlas.services.inventory.port.in.model.StockOutput;
import org.atlas.services.inventory.port.in.service.StockAdminService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stocks/admin")
@Validated
@RequiredArgsConstructor
public class StockAdminController {

  private final StockAdminService stockAdminService;

  @GetMapping(value = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve stock information for a product")
  public ApiResponseWrapper<RetrieveStockResponse> retrieveStock(
      @Parameter(name = "productId", description = "ID of the product to retrieve stock for", example = "123")
      @PathVariable String productId) {
    StockOutput output = stockAdminService.retrieveStock(productId);
    RetrieveStockResponse responseData = StockAdminMapper.INSTANCE.toRetrieveStockResponse(output);
    return ApiResponseWrapper.success(responseData);
  }

  @PutMapping(value = "/{productId}/available-quantity", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Update available quantity for a product")
  public ApiResponseWrapper<Void> updateAvailableQuantity(
      @Parameter(name = "productId", description = "ID of the product to update available quantity for", example = "123")
      @PathVariable String productId,
      @Parameter(description = "Update available quantity request", required = true)
      @Valid @RequestBody UpdateAvailableQuantityRequest request) {
    stockAdminService.updateAvailableQuantity(productId, request.getAvailableQuantity());
    return ApiResponseWrapper.success(null);
  }
}

package org.atlas.services.inventory.infrastructure.api.server.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.internal.inventory.model.RetrieveStockListInput;
import org.atlas.libs.framework.internal.inventory.model.StockOutput;
import org.atlas.services.inventory.infrastructure.api.server.rest.mapper.StockInternalMapper;
import org.atlas.services.inventory.infrastructure.api.server.rest.model.internal.RetrieveStockListRequest;
import org.atlas.services.inventory.port.in.service.StockInternalService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stocks/internal")
@Validated
@RequiredArgsConstructor
public class StockInternalController {

  private final StockInternalService stockInternalService;

  @PostMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a list of stocks based on specified criteria")
  public ApiResponseWrapper<List<StockOutput>> retrieveProductList(
      @Parameter(description = "Request object containing the criteria for listing stocks", required = true)
      @Valid @RequestBody RetrieveStockListRequest request) {
    RetrieveStockListInput input = StockInternalMapper.INSTANCE.toRetrieveStockListInput(request);
    List<StockOutput> responseData = stockInternalService.retrieveStockList(input);
    return ApiResponseWrapper.success(responseData);
  }
}

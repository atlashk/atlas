package org.atlas.services.catalog.infrastructure.api.server.rest.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.internal.product.model.ProductOutput;
import org.atlas.libs.framework.internal.product.model.RetrieveProductListInput;
import org.atlas.services.catalog.infrastructure.api.server.rest.product.mapper.ProductInternalMapper;
import org.atlas.services.catalog.infrastructure.api.server.rest.product.model.internal.RetrieveProductListRequest;
import org.atlas.services.catalog.port.in.product.service.ProductInternalService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products/internal")
@Validated
@RequiredArgsConstructor
public class ProductInternalController {

  private final ProductInternalService productInternalService;

  @PostMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a list of products based on specified criteria")
  public ApiResponseWrapper<List<ProductOutput>> retrieveProductList(
      @Parameter(description = "Request object containing the criteria for listing products", required = true)
      @Valid @RequestBody RetrieveProductListRequest request) {
    RetrieveProductListInput input = ProductInternalMapper.INSTANCE
        .toRetrieveProductListInput(request);
    List<ProductOutput> responseData = productInternalService.retrieveProductList(input);
    return ApiResponseWrapper.success(responseData);
  }
}

package org.atlas.infrastructure.api.server.rest.adapter.product.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.application.product.internal.model.InternalRetrieveProductListInput;
import org.atlas.application.product.internal.service.InternalProductService;
import org.atlas.domain.product.entity.Product;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.internalapi.product.model.ProductResponse;
import org.atlas.framework.util.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.rest.adapter.product.internal.mapper.InternalProductMapper;
import org.atlas.infrastructure.api.server.rest.adapter.product.internal.model.InternalRetrieveProductListRequest;
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
public class InternalProductController {

  private final InternalProductService internalProductService;

  @PostMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a list of products based on specified criteria")
  public ApiResponseWrapper<List<ProductResponse>> retrieveProductList(
      @Parameter(description = "Request object containing the criteria for listing products", required = true)
      @Valid @RequestBody InternalRetrieveProductListRequest request) {
    InternalRetrieveProductListInput input = InternalProductMapper.INSTANCE
        .toInternalRetrieveProductListInput(request);
    List<Product> products = internalProductService.retrieveProductList(input);
    List<ProductResponse> responseData = ObjectMapperUtil.mapList(products,
        InternalProductMapper.INSTANCE::toProductResponse);
    return ApiResponseWrapper.success(responseData);
  }
}

package org.atlas.services.product.api.server.rest.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.internalapi.product.model.ProductResponse;
import org.atlas.libs.framework.util.ObjectMapperUtil;
import org.atlas.services.product.api.server.rest.internal.mapper.InternalProductMapper;
import org.atlas.services.product.api.server.rest.internal.model.InternalRetrieveProductListRequest;
import org.atlas.services.product.application.internal.model.InternalRetrieveProductListInput;
import org.atlas.services.product.application.internal.service.InternalProductService;
import org.atlas.services.product.domain.entity.Product;
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

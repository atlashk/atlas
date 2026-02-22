package org.atlas.services.catalog.infrastructure.api.server.rest.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.services.catalog.domain.entity.ProductEntity;
import org.atlas.services.catalog.infrastructure.api.server.rest.product.mapper.ProductMapper;
import org.atlas.services.catalog.infrastructure.api.server.rest.product.model.ProductResponse;
import org.atlas.services.catalog.port.in.product.service.ProductService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/front/products")
@Validated
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve details of a specific product by ID")
  public ApiResponseWrapper<ProductResponse> retrieveProduct(
      @Parameter(name = "id", description = "The unique identifier of the product.", example = "1", required = true)
      @PathVariable String id) throws Exception {
    ProductEntity product = productService.retrieveProduct(id);
    ProductResponse responseData = ProductMapper.INSTANCE.toProductResponse(product);
    return ApiResponseWrapper.success(responseData);
  }
}

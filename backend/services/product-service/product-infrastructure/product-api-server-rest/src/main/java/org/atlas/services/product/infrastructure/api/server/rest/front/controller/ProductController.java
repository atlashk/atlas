package org.atlas.services.product.infrastructure.api.server.rest.front.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.product.domain.entity.ProductEntity;
import org.atlas.services.product.infrastructure.api.server.rest.front.mapper.ProductMapper;
import org.atlas.services.product.infrastructure.api.server.rest.front.model.ProductResponse;
import org.atlas.services.product.infrastructure.api.server.rest.front.model.RetrieveProductListRequest;
import org.atlas.services.product.port.in.front.model.RetrieveProductListInput;
import org.atlas.services.product.port.in.front.service.ProductService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/front/products")
@Validated
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a list of products based on various filters")
  public ApiResponseWrapper<List<ProductResponse>> retrieveProductList(
      @Parameter(description = "Request object containing filters and pagination", required = true)
      @Valid @RequestBody RetrieveProductListRequest request
  ) {
    RetrieveProductListInput input = RetrieveProductListInput.builder()
        .keyword(request.getKeyword())
        .minPrice(request.getMinPrice())
        .maxPrice(request.getMaxPrice())
        .brandId(request.getBrandId())
        .categoryIds(request.getCategoryIds())
        .pagingRequest(PagingRequest.of(request.getPage() - 1, request.getSize()))
        .mode(request.getMode())
        .build();
    PagingResult<ProductEntity> productPage = productService.retrieveProductList(input);
    PagingResult<ProductResponse> responseData = MapperUtil.mapPage(productPage,
        ProductMapper.INSTANCE::toProductResponse);
    return ApiResponseWrapper.successPage(responseData);
  }

  @GetMapping(value = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve details of a specific product by ID")
  public ApiResponseWrapper<ProductResponse> retrieveProduct(
      @Parameter(name = "productId", description = "The unique identifier of the product.", example = "1", required = true)
      @PathVariable String productId) throws Exception {
    ProductEntity product = productService.retrieveProduct(productId);
    ProductResponse responseData = ProductMapper.INSTANCE.toProductResponse(product);
    return ApiResponseWrapper.success(responseData);
  }
}

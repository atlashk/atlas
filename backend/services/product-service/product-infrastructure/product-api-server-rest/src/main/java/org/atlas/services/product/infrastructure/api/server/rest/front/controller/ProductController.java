package org.atlas.services.product.infrastructure.api.server.rest.front.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.constant.CommonConstant;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.ObjectMapperUtil;
import org.atlas.services.product.infrastructure.api.server.rest.front.mapper.ProductMapper;
import org.atlas.services.product.infrastructure.api.server.rest.front.model.ProductResponse;
import org.atlas.services.product.port.in.front.model.RetrieveProductListInput;
import org.atlas.services.product.port.in.front.service.ProductService;
import org.atlas.services.product.domain.entity.ProductEntity;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/front/products")
@Validated
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a list of products based on various filters")
  public ApiResponseWrapper<List<ProductResponse>> retrieveProductList(
      @Parameter(name = "keyword", description = "Keyword for searching products.", example = "T-Shirt")
      @RequestParam(name = "keyword", required = false) String keyword,
      @Parameter(name = "minPrice", description = "Minimum price for filtering products.", example = "10.00")
      @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
      @Parameter(name = "maxPrice", description = "Maximum price for filtering products.", example = "100.00")
      @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
      @Parameter(name = "brandId", description = "Brand ID for filtering products.", example = "1")
      @RequestParam(name = "brandId", required = false) Integer brandId,
      @Parameter(name = "categoryIds", description = "List of category IDs for filtering products.", example = "[1, 2, 3]")
      @RequestParam(name = "categoryIds", required = false) List<Integer> categoryIds,
      @Parameter(name = "page", description = "Page number for pagination.", example = "1")
      @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
      @Parameter(name = "size", description = "Number of items per page.", example = "20")
      @RequestParam(name = "size", required = false, defaultValue = CommonConstant.DEFAULT_PAGE_SIZE_STR) Integer size
  ) {
    RetrieveProductListInput input = RetrieveProductListInput.builder()
        .keyword(keyword)
        .minPrice(minPrice)
        .maxPrice(maxPrice)
        .brandId(brandId)
        .categoryIds(categoryIds)
        .pagingRequest(PagingRequest.of(page - 1, size))
        .build();
    PagingResult<ProductEntity> productPage = productService.retrieveProductList(input);
    PagingResult<ProductResponse> responseData = ObjectMapperUtil.mapPage(productPage,
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

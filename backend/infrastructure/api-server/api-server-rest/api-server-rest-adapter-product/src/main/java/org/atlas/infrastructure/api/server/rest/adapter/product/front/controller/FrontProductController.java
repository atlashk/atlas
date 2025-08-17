package org.atlas.infrastructure.api.server.rest.adapter.product.front.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.usecase.front.handler.FrontGetProductUseCaseHandler;
import org.atlas.domain.product.usecase.front.handler.FrontSearchProductUseCaseHandler;
import org.atlas.domain.product.usecase.front.model.FrontSearchProductInput;
import org.atlas.framework.api.server.rest.response.ApiResponseWrapper;
import org.atlas.framework.constant.CommonConstant;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingResult;
import org.atlas.infrastructure.api.server.rest.adapter.product.shared.ProductResponse;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/front/products")
@Validated
@RequiredArgsConstructor
public class FrontProductController {

  private final FrontSearchProductUseCaseHandler frontSearchProductUseCaseHandler;
  private final FrontGetProductUseCaseHandler frontGetProductUseCaseHandler;

  @Operation(summary = "Search for products based on various filters.")
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseWrapper<List<ProductResponse>> searchProduct(
      @Parameter(description = "Keyword for searching products.", example = "T-Shirt")
      @RequestParam(name = "keyword", required = false) String keyword,
      @Parameter(description = "Minimum price for filtering products.", example = "10.00")
      @RequestParam(name = "min_price", required = false) BigDecimal minPrice,
      @Parameter(description = "Maximum price for filtering products.", example = "100.00")
      @RequestParam(name = "max_price", required = false) BigDecimal maxPrice,
      @Parameter(description = "Brand ID for filtering products.", example = "1")
      @RequestParam(name = "brand_id", required = false) Integer brandId,
      @Parameter(description = "List of category IDs for filtering products.", example = "[1, 2, 3]")
      @RequestParam(name = "category_ids", required = false) List<Integer> categoryIds,
      @Parameter(description = "Page number for pagination.", example = "1")
      @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
      @Parameter(description = "Number of items per page.", example = "20")
      @RequestParam(name = "size", required = false, defaultValue = CommonConstant.DEFAULT_PAGE_SIZE_STR) Integer size
  ) throws Exception {
    FrontSearchProductInput input = FrontSearchProductInput.builder()
        .keyword(keyword)
        .minPrice(minPrice)
        .maxPrice(maxPrice)
        .brandId(brandId)
        .categoryIds(categoryIds)
        .pagingRequest(PagingRequest.of(page - 1, size))
        .build();

    PagingResult<ProductEntity> productEntityPage = frontSearchProductUseCaseHandler.handle(input);

    List<ProductResponse> productResponses = productEntityPage.getData()
        .stream()
        .map(productEntity -> {
          ProductResponse productResponse = new ProductResponse();
          productResponse.setId(productEntity.getId());
          productResponse.setName(productEntity.getName());
          productResponse.setPrice(productEntity.getPrice());
          productResponse.setImage(productEntity.getImage());
          return productResponse;
        })
        .toList();
    PagingResult<ProductResponse> productResponsePage = PagingResult.of(productResponses,
        productEntityPage.getPagination());
    return ApiResponseWrapper.successPage(productResponsePage);
  }

  @Operation(summary = "Retrieve details of a specific product by ID.")
  @GetMapping(value = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseWrapper<ProductResponse> getProduct(
      @Parameter(description = "The unique identifier of the product.", example = "1", required = true)
      @PathVariable("productId") Integer productId) throws Exception {
    ProductEntity productEntity = frontGetProductUseCaseHandler.handle(productId);
    ProductResponse response = ObjectMapperUtil.getInstance()
        .map(productEntity, ProductResponse.class);
    return ApiResponseWrapper.success(response);
  }
}

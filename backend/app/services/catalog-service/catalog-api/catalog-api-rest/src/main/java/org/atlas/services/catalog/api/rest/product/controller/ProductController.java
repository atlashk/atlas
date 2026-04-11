package org.atlas.services.catalog.api.rest.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.rest.ApiResponseWrapper;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.catalog.api.rest.product.mapper.ProductMapper;
import org.atlas.services.catalog.api.rest.product.model.ProductResponse;
import org.atlas.services.catalog.api.rest.product.model.RetrieveProductListRequest;
import org.atlas.services.catalog.domain.entity.Product;
import org.atlas.services.catalog.port.in.product.model.RetrieveProductListInput;
import org.atlas.services.catalog.port.in.product.service.ProductService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/products")
@Validated
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a list of products based on various filters")
  public ApiResponseWrapper<List<ProductResponse>> retrieveProductList(
      @Parameter(description = "Request object containing filters and pagination", required = true)
      @Valid @RequestBody RetrieveProductListRequest request
  ) {
    RetrieveProductListInput input = ProductMapper.INSTANCE.toRetrieveProductListInput(request);
    input.setPagingRequest(PagingRequest.of(request.getPage() - 1, request.getSize()));

    PagingResult<Product> productPage = productService.retrieveProductList(input);
    PagingResult<ProductResponse> responseData = MapperUtil.mapPage(productPage,
        ProductMapper.INSTANCE::toProductResponse);
    return ApiResponseWrapper.successPage(responseData);
  }

  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve details of a specific product by ID")
  public ApiResponseWrapper<ProductResponse> retrieveProduct(
      @Parameter(name = "id", description = "The unique identifier of the product.", example = "1", required = true)
      @PathVariable String id) throws Exception {
    Product product = productService.retrieveProduct(id);
    ProductResponse responseData = ProductMapper.INSTANCE.toProductResponse(product);
    return ApiResponseWrapper.success(responseData);
  }
}

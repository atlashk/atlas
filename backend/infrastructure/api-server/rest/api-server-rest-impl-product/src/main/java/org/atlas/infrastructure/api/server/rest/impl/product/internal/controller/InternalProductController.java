package org.atlas.infrastructure.api.server.rest.impl.product.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.Product;
import org.atlas.domain.product.usecase.internal.handler.InternalListProductUseCaseHandler;
import org.atlas.domain.product.usecase.internal.model.InternalListProductInput;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.util.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.rest.impl.product.front.model.ProductResponse;
import org.atlas.infrastructure.api.server.rest.impl.product.internal.model.InternalListProductRequest;
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

  private final InternalListProductUseCaseHandler internalListProductUseCaseHandler;

  @PostMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a list of products based on specified criteria")
  public ApiResponseWrapper<List<ProductResponse>> listProduct(
      @Parameter(description = "Request object containing the criteria for listing products", required = true)
      @Valid @RequestBody InternalListProductRequest request) throws Exception {
    InternalListProductInput input = ObjectMapperUtil.getInstance()
        .map(request, InternalListProductInput.class);
    List<Product> products = internalListProductUseCaseHandler.handle(input);
    List<ProductResponse> productResponses = ObjectMapperUtil.getInstance()
        .mapList(products, ProductResponse.class);
    return ApiResponseWrapper.success(productResponses);
  }
}

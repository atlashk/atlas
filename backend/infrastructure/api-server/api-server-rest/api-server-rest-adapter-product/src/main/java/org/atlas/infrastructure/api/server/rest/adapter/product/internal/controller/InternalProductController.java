package org.atlas.infrastructure.api.server.rest.adapter.product.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.usecase.internal.handler.InternalListProductUseCaseHandler;
import org.atlas.domain.product.usecase.internal.model.InternalListProductInput;
import org.atlas.framework.api.server.rest.response.ApiResponseWrapper;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.rest.adapter.product.internal.model.InternalListProductRequest;
import org.atlas.infrastructure.api.server.rest.adapter.product.shared.ProductResponse;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/internal/products")
@Validated
@RequiredArgsConstructor
public class InternalProductController {

  private final InternalListProductUseCaseHandler internalListProductUseCaseHandler;

  @Operation(summary = "List products", description = "Retrieves a list of products based on the provided criteria.")
  @PostMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseWrapper<List<ProductResponse>> listProduct(
      @Parameter(description = "Request object containing the criteria for listing products.", required = true)
      @Valid @RequestBody InternalListProductRequest request) throws Exception {
    InternalListProductInput input = ObjectMapperUtil.getInstance()
        .map(request, InternalListProductInput.class);
    List<ProductEntity> productEntities = internalListProductUseCaseHandler.handle(input);
    List<ProductResponse> productResponses = productEntities.stream()
        .map(productEntity -> {
          ProductResponse productResponse = new ProductResponse();
          productResponse.setId(productEntity.getId());
          productResponse.setName(productEntity.getName());
          productResponse.setPrice(productEntity.getPrice());
          productResponse.setImage(productEntity.getImage());
          return productResponse;
        })
        .toList();
    return ApiResponseWrapper.success(productResponses);
  }
}

package org.atlas.infrastructure.api.server.rest.adapter.product.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.BrandEntity;
import org.atlas.domain.product.usecase.common.handler.ListBrandUseCaseHandler;
import org.atlas.framework.api.server.rest.response.ApiResponseWrapper;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.rest.adapter.product.shared.BrandResponse;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/common/products/brands")
@Validated
@RequiredArgsConstructor
public class CommonBrandController {

  private final ListBrandUseCaseHandler listBrandUseCaseHandler;

  @Operation(summary = "List Brands", description = "Retrieves a list of all available brands.")
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseWrapper<List<BrandResponse>> listBrand() throws Exception {
    List<BrandEntity> brandEntities = listBrandUseCaseHandler.handle(null);
    List<BrandResponse> brandResponses = ObjectMapperUtil.getInstance()
        .mapList(brandEntities, BrandResponse.class);
    return ApiResponseWrapper.success(brandResponses);
  }
}
